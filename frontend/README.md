# Frontend

The React frontend of [expense_splitter](../README.md), written in TypeScript and built with Vite.

| Command | What it does |
|---|---|
| `npm install` | Install the dependencies |
| `npm run dev` | Start the dev server on http://localhost:5173 (forwards `/api` to the backend on port 8080) |
| `npm test` | Run the tests with Vitest |
| `npm run lint` | Check the code with oxlint |
| `npm run build` | Type-check and build the app into `dist/` |

- `src/api.ts` has every call to the backend.
- `src/money.ts` converts between cents and the text shown in the app: 1234 becomes "12,34 €", and "12,34" typed by the user becomes 1234.
- Each page (`GroupsPage`, `GroupPage`, `BalancesPage`) has its own CSS file and test file next to it.

In Docker, [nginx.conf](nginx.conf) serves the built app and forwards `/api` to the backend.
