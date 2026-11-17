import { Route, Routes } from 'react-router'
import BalancesPage from './BalancesPage'
import GroupPage from './GroupPage'
import GroupsPage from './GroupsPage'

function App() {
  return (
    <main>
      <h1>expense_splitter</h1>
      {/* Routes shows the first Route whose path matches the current URL. */}
      <Routes>
        <Route path="/" element={<GroupsPage />} />
        <Route path="/groups/:groupId" element={<GroupPage />} />
        <Route path="/groups/:groupId/balances" element={<BalancesPage />} />
      </Routes>
    </main>
  )
}

export default App
