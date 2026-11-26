// The service worker of expense_splitter: a small script that the browser keeps running in the
// background for our site. It saves the app's own files (HTML, icons, manifest), so the app opens
// quickly and even without internet. Data from the API is never saved here.

// Change this name whenever the list of saved files changes: the browser then installs the new
// version and the "activate" step below deletes the old saved files.
const CACHE_NAME = 'expense-splitter-v1'

// The files every page needs. The JavaScript and CSS files have a new name after every build,
// so they are saved later, the first time they are loaded.
const APP_SHELL = [
  '/',
  '/manifest.webmanifest',
  '/icon.svg',
  '/icon-192.png',
  '/icon-512.png',
  '/icon-maskable-512.png',
  '/apple-touch-icon.png',
]

// Runs once when the browser installs this service worker: save the app shell.
self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL)))
  // Don't wait until every open tab is closed; the new version takes over right away.
  self.skipWaiting()
})

// Runs when this version becomes the active one: delete the saved files of older versions.
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((names) =>
        Promise.all(names.filter((name) => name !== CACHE_NAME).map((name) => caches.delete(name))),
      )
      .then(() => self.clients.claim()),
  )
})

// Requests that always go to the server and are never saved: the API (the data must be fresh and
// belongs to one logged-in user), the Google login and the logout.
const NEVER_CACHE = ['/api/', '/oauth2/', '/login/oauth2/', '/logout']

// Saves a copy of a good answer for later, without making the page wait for it.
function remember(key, response) {
  if (response.ok) {
    const copy = response.clone()
    caches.open(CACHE_NAME).then((cache) => cache.put(key, copy))
  }
  return response
}

// Runs for every request of our pages and decides where the answer comes from.
self.addEventListener('fetch', (event) => {
  const request = event.request
  const url = new URL(request.url)

  // Only reading requests (GET) to our own site. Everything else (POST, DELETE, other websites)
  // goes to the network untouched, because "return" without respondWith means "do nothing".
  if (request.method !== 'GET' || url.origin !== self.location.origin) {
    return
  }
  if (NEVER_CACHE.some((path) => url.pathname.startsWith(path))) {
    return
  }

  if (request.mode === 'navigate') {
    // Opening a page: network first, so you always get the newest version. Every page of the app
    // is the same index.html, so it is saved under "/". Without internet, that saved copy is used.
    event.respondWith(
      fetch(request)
        .then((response) => remember('/', response))
        .catch(() => caches.match('/')),
    )
    return
  }

  // Files (JavaScript, CSS, icons): the saved copy first, otherwise load and save it.
  // That is safe because Vite gives these files a new name whenever their content changes.
  event.respondWith(
    caches.match(request).then((cached) => cached ?? fetch(request).then((response) => remember(request, response))),
  )
})
