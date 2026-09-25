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
import TestSellerPage from './pages/TestSellerPage';
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
        {/* Shop onboarding and management: the shop id is always in the URL */}
        <Route path="/shops" element={<MyBusinessesPage />} />
        <Route path="/shops/new" element={<CreateShopPage />} />
        <Route path="/shops/:shopId" element={<DashboardPage />} />
        <Route path="/shops/:shopId/catalog" element={<FillCatalogPage />} />
        <Route path="/shops/:shopId/test" element={<TestSellerPage />} />
        <Route path="/shops/:shopId/connect" element={<ConnectChannelsPage />} />
        {/* Old shop routes, kept so existing links keep working */}
        <Route path="/my-businesses" element={<Navigate to="/shops" replace />} />
        <Route path="/create-shop" element={<Navigate to="/shops/new" replace />} />
        <Route path="/fill-catalog" element={<Navigate to="/shops" replace />} />
        <Route path="/connect-channels" element={<Navigate to="/shops" replace />} />
        <Route path="/shop-dashboard" element={<Navigate to="/shops" replace />} />
        <Route path="/old-dashboard" element={<Navigate to="/shops" replace />} />
        {/* Unified Event page routes */}
        <Route path="/create-event" element={<EventPage />} />
        <Route path="/events/:eventId" element={<EventPage />} />
        <Route path="/create-clinic" element={<CreateClinicPage />} />
        <Route path="/dashboard" element={<EventsDashboardPage />} />
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
