/*
 * Local, backend-free stand-in for a real auth provider (e.g. Amazon Cognito).
 * UbiGrid's API layer isn't built yet, so this stores users and the current
 * session in localStorage purely to demo the register -> verify -> sign in ->
 * request flow end to end. Swap this module for real calls once the API exists.
 */
var UbiGrid = window.UbiGrid || {};
UbiGrid.auth = (function auth() {
  var USERS_KEY = 'ubigrid_users';
  var SESSION_KEY = 'ubigrid_session';

  function loadUsers() {
    try {
      return JSON.parse(localStorage.getItem(USERS_KEY)) || {};
    } catch (e) {
      return {};
    }
  }

  function saveUsers(users) {
    localStorage.setItem(USERS_KEY, JSON.stringify(users));
  }

  function generateCode() {
    return String(Math.floor(100000 + Math.random() * 900000));
  }

  function register(email, password) {
    var users = loadUsers();
    if (users[email]) {
      throw new Error('Ya existe una cuenta con ese correo.');
    }
    var code = generateCode();
    users[email] = { password: password, verified: false, code: code };
    saveUsers(users);
    return code;
  }

  function verify(email, code) {
    var users = loadUsers();
    var user = users[email];
    if (!user || user.code !== code) {
      throw new Error('Código de verificación inválido.');
    }
    user.verified = true;
    saveUsers(users);
  }

  function signIn(email, password) {
    var users = loadUsers();
    var user = users[email];
    if (!user || user.password !== password) {
      throw new Error('Correo o contraseña incorrectos.');
    }
    if (!user.verified) {
      throw new Error('Debes verificar tu correo antes de iniciar sesión.');
    }
    localStorage.setItem(SESSION_KEY, email);
  }

  function signOut() {
    localStorage.removeItem(SESSION_KEY);
  }

  function currentUser() {
    return localStorage.getItem(SESSION_KEY);
  }

  // Signs in as the account named in the `autologin` query parameter
  // (e.g. request.html?autologin=user@mail.com), creating it verified with a
  // random password if it doesn't exist yet. Returns the email, or null when
  // the URL carries no autologin parameter.
  function autoLogin() {
    var email = new URLSearchParams(window.location.search).get('autologin');
    if (!email) {
      return null;
    }
    var users = loadUsers();
    if (!users[email]) {
      users[email] = { password: generateCode() + generateCode(), verified: true, code: null };
      saveUsers(users);
    }
    localStorage.setItem(SESSION_KEY, email);
    return email;
  }

  // Guarantees a session exists for the current page: an `autologin` URL
  // parameter takes precedence, otherwise an existing session is kept, and
  // with neither the visitor is sent to redirectTo.
  function requireSession(redirectTo) {
    autoLogin();
    if (!currentUser()) {
      window.location.href = redirectTo;
    }
    return currentUser();
  }

  return {
    register: register,
    verify: verify,
    signIn: signIn,
    signOut: signOut,
    currentUser: currentUser,
    autoLogin: autoLogin,
    requireSession: requireSession
  };
}());
