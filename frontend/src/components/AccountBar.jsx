import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { LogOut, Shield, UserRound } from 'lucide-react';
import api from '../api/api.ts';

// Pages without a logged-in user
const PUBLIC_PATHS = ['/', '/login', '/register', '/terms'];

// Thin bar above every page after login: which account is logged in, admin mark, logout.
// One browser holds one session, so this is how you tell accounts apart when switching between them.
const AccountBar = () => {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const [account, setAccount] = useState(null); // { email, admin }
  const wasPublic = useRef(true);
  const isPublic = PUBLIC_PATHS.includes(pathname);

  useEffect(() => {
    // Ask the server again after every login or account switch (coming from a public page)
    if (isPublic) {
      wasPublic.current = true;
      return;
    }
    if (!wasPublic.current && account) return;
    wasPublic.current = false;
    Promise.all([
      api.get('/api/v1/auth/me'),
      api.get('/api/v1/admin/me').catch(() => ({ data: { admin: false } })),
    ])
      .then(([me, admin]) => setAccount({ email: me.data.email, admin: admin.data.admin === true }))
      .catch(() => setAccount(null));
  }, [isPublic, pathname, account]);

  const logout = async () => {
    try {
      await api.post('/api/v1/auth/logout');
    } finally {
      setAccount(null);
      sessionStorage.clear();
      navigate('/login', { replace: true });
    }
  };

  if (isPublic || !account) return null;

  return (
    <div className="bg-slate-900 text-slate-100 text-sm">
      <div className="max-w-7xl mx-auto px-4 h-9 flex items-center justify-end gap-3">
        <span className="flex items-center gap-1.5 min-w-0" title={account.email}>
          <UserRound className="w-4 h-4 shrink-0 text-slate-400" />
          <span className="truncate max-w-[45vw] sm:max-w-none">{account.email}</span>
        </span>
        {account.admin && (
          <button type="button" onClick={() => navigate('/admin')}
                  className="flex items-center gap-1 bg-amber-400 text-slate-900 font-semibold rounded-full px-2 py-0.5 text-xs hover:bg-amber-300">
            <Shield className="w-3 h-3" />
            Админ
          </button>
        )}
        <button type="button" onClick={logout}
                className="flex items-center gap-1 text-slate-300 hover:text-white">
          <LogOut className="w-4 h-4" />
          <span className="hidden sm:inline">Выйти</span>
        </button>
      </div>
    </div>
  );
};

export default AccountBar;
