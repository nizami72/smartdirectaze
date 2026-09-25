import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api.ts';

import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Container,
  // Grid,
  Stack,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';

const pluralized = (count, industry) => {
  const word = industry === 'EVENTS' ? 'event' : 'item';
  const suffix = count === 1 ? '' : 's';
  return `${count} ${word}${suffix}`;
};

const MyBusinessesPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cards, setCards] = useState([]);

  useEffect(() => {
    let isMounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        // Per-industry summaries + the user's shops: every shop gets its own card
        const [businessesRes, shopsRes] = await Promise.all([
          api.get('/api/v1/businesses/my'),
          api.get('/api/v1/shops/my'),
        ]);
        const shopCards = (shopsRes.data || []).map((shop) => ({
          key: `shop-${shop.id}`,
          title: shop.shopName,
          subtitle: `Shop #${shop.id}`,
          path: `/shops/${shop.id}`,
        }));
        // Other industries (events) keep their summary card
        const otherCards = (businessesRes.data || []).filter((b) => b.industry !== 'SHOP').map((b) => {
          const display = b.displayName || (b.industry === 'EVENTS' ? 'Events' : b.industry === 'DENTAL' ? 'Dental' : (b.industry || 'Business'));
          return {
            key: b.industry,
            title: display,
            subtitle: pluralized(b.count ?? 0, b.industry),
            path: '/dashboard',
          };
        });
        const items = [...shopCards, ...otherCards];
        if (items.length === 0) {
          // Nothing yet: straight to creating the first shop
          navigate('/shops/new', { replace: true });
          return;
        }
        if (isMounted) setCards(items);
      } catch (e) {
        console.error('Failed to load businesses', e);
        if (isMounted) setError('Failed to load your businesses. Please try again.');
      } finally {
        if (isMounted) setLoading(false);
      }
    };
    load();
    return () => {
      isMounted = false;
    };
  }, [navigate]);

  const handleCreateShop = () => {
    navigate('/shops/new');
  };

  const hasBusinesses = cards.length > 0;

  return (
    <Container maxWidth="lg" sx={{ py: { xs: 3, md: 6 } }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 3 }}>
        <Typography variant="h4" fontWeight={700} gutterBottom sx={{ mb: 0 }}>
          Мои магазины
        </Typography>

        {hasBusinesses && (
          <Button variant="text" color="primary" onClick={handleCreateShop}>
            + Новый магазин
          </Button>
        )}
      </Stack>

      {/* Empty state */}
      {!loading && !error && !hasBusinesses && (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            textAlign: 'center',
            py: 8,
            px: 2,
          }}
        >
          <Typography variant="h5" fontWeight={700} gutterBottom>
            У вас пока нет магазинов.
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Создайте первый магазин — это займёт минуту.
          </Typography>
          <Button variant="contained" color="primary" size="large" onClick={handleCreateShop}>
            Создать магазин
          </Button>
        </Box>
      )}

      {/* Error state */}
      {!loading && error && (
        <Box sx={{ py: 4 }}>
          <Typography color="error" align="center">{error}</Typography>
          <Stack direction="row" justifyContent="center" sx={{ mt: 2 }}>
            <Button onClick={() => window.location.reload()}>Retry</Button>
          </Stack>
        </Box>
      )}

      {/* List state */}
      {!loading && !error && hasBusinesses && (
        <Grid container spacing={3}>
          {cards.map((c) => (
            <Grid key={c.key} size={{ xs: 12, sm: 6, md: 4 }}>
              <Card elevation={2} sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="h6" fontWeight={700} gutterBottom>
                    {c.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {c.subtitle}
                  </Typography>
                </CardContent>
                <CardActions sx={{ px: 2, pb: 2 }}>
                  <Button variant="contained" onClick={() => navigate(c.path)}>
                    Open
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default MyBusinessesPage;
