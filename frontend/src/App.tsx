import { Link, Route, Routes } from 'react-router'
import './App.css'
import BalancesPage from './BalancesPage'
import GroupPage from './GroupPage'
import GroupsPage from './GroupsPage'
import ThemeToggle from './ThemeToggle'

function App() {
  return (
    <>
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
        </Routes>
      </main>
    </>
  )
}

export default App