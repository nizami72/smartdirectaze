import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';
import ChooseBusinessPage from './pages/ChooseBusinessPage';
import MyBusinessesPage from './pages/MyBusinessesPage';
import CreateShopPage from './pages/CreateShopPage';
import EventPage from './features/events/pages/EventPage';
import CreateClinicPage from './pages/CreateClinicPage';
import FillCatalogPage from './pages/FillCatalogPage';
import ConnectChannelsPage from './pages/ConnectChannelsPage';
import DashboardPage from './pages/DashboardPage';
import EventsDashboardPage from './features/dashboard/pages/EventsDashboardPage';
import './App.css';
import GuestListPage from './features/guests/pages/GuestListPage';
import GuestPage from './features/guests/pages/GuestPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/choose-business" element={<ChooseBusinessPage />} />
        <Route path="/my-businesses" element={<MyBusinessesPage />} />
        <Route path="/create-shop" element={<CreateShopPage />} />
        {/* Unified Event page routes */}
        <Route path="/create-event" element={<EventPage />} />
        <Route path="/events/:eventId" element={<EventPage />} />
        <Route path="/create-clinic" element={<CreateClinicPage />} />
        <Route path="/fill-catalog" element={<FillCatalogPage />} />
        <Route path="/connect-channels" element={<ConnectChannelsPage />} />
        <Route path="/dashboard" element={<EventsDashboardPage />} />
        <Route path="/old-dashboard" element={<DashboardPage />} />
        {/* Guests: business and event contexts (same page) */}
        <Route path="/business/:businessId/guests" element={<GuestListPage />} />
        <Route path="/events/:eventId/guests" element={<GuestListPage />} />
        <Route path="/guests" element={<GuestListPage />} />
        <Route path="/business/:businessId/events/:eventId/guests" element={<GuestListPage />} />
        {/* Guest single page: Create/View/Edit */}
        <Route path="/business/:businessId/guests/new" element={<GuestPage />} />
        <Route path="/guest/new" element={<GuestPage />} />
        <Route path="/business/:businessId/guests/:guestId" element={<GuestPage />} />
        {/* Редирект с корня на регистрацию для удобства */}
        <Route path="/" element={<Navigate to="/register" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
