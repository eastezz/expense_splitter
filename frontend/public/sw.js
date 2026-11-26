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
