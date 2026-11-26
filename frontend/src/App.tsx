import { Link, Route, Routes } from 'react-router'
import './App.css'
import AuthProvider from './AuthProvider'
import BalancesPage from './BalancesPage'
import GroupPage from './GroupPage'
import GroupsPage from './GroupsPage'
import InstallButton from './InstallButton'
import IosInstallHint from './IosInstallHint'
import LoginPage from './LoginPage'
import RegisterPage from './RegisterPage'
import RequireLogin from './RequireLogin'
import ThemeToggle from './ThemeToggle'
import ToastProvider from './ToastProvider'
import UserMenu from './UserMenu'

function App() {
  return (
    // ToastProvider lets every page show short messages like "Expense added".
    // AuthProvider tells every page who is logged in.
    <ToastProvider>
      <AuthProvider>
        {/* The bar on top of every page: the app name (a link to the start page),
            "Install app" when the browser offers it, the logged-in user with "Log out",
            and the theme button. */}
        <header className="app-header">
          <h1 className="app-name">
            <Link to="/">expense_splitter</Link>
          </h1>
          <div className="header-actions">
            <InstallButton />
            <UserMenu />
            <ThemeToggle />
          </div>
        </header>
        <main>
          {/* Only on iPhones and iPads: how to install the app from Safari. */}
          <IosInstallHint />
          {/* Routes shows the first Route whose path matches the current URL.
              The pages with groups and money are wrapped in RequireLogin. */}
          <Routes>
            <Route
              path="/"
              element={
                <RequireLogin>
                  <GroupsPage />
                </RequireLogin>
              }
            />
            <Route
              path="/groups/:groupId"
              element={
                <RequireLogin>
                  <GroupPage />
                </RequireLogin>
              }
            />
            <Route
              path="/groups/:groupId/balances"
              element={
                <RequireLogin>
                  <BalancesPage />
                </RequireLogin>
              }
            />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Routes>
        </main>
      </AuthProvider>
    </ToastProvider>
  )
}

export default App
