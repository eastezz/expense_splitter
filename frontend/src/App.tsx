import { Link, Route, Routes } from 'react-router'
import './App.css'
import BalancesPage from './BalancesPage'
import GroupPage from './GroupPage'
import GroupsPage from './GroupsPage'
import LoginPage from './LoginPage'
import RegisterPage from './RegisterPage'
import ThemeToggle from './ThemeToggle'
import ToastProvider from './ToastProvider'

function App() {
  return (
    // ToastProvider lets every page show short messages like "Expense added".
    <ToastProvider>
      {/* The bar on top of every page: the app name (a link to the start page) and the theme button. */}
      <header className="app-header">
        <h1 className="app-name">
          <Link to="/">expense_splitter</Link>
        </h1>
        <ThemeToggle />
      </header>
      <main>
        {/* Routes shows the first Route whose path matches the current URL. */}
        <Routes>
          <Route path="/" element={<GroupsPage />} />
          <Route path="/groups/:groupId" element={<GroupPage />} />
          <Route path="/groups/:groupId/balances" element={<BalancesPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Routes>
      </main>
    </ToastProvider>
  )
}

export default App