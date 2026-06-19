import React, { useState, lazy, Suspense } from 'react';
import Box from '@mui/material/Box';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Drawer from '@mui/material/Drawer';
import Container from '@mui/material/Container';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import Button from '@mui/material/Button';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import CircularProgress from '@mui/material/CircularProgress';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import SettingsIcon from '@mui/icons-material/Settings';

import { getTheme, type ThemeMode } from './theme';
import { AuthProvider, useAuth, Login, UserProfileModal } from './features/auth';
import { NotificationProvider } from './context/NotificationContext';
import { ModuleLoader } from './components/ModuleLoader';

import smlLogo from './assets/sml_Go.png';

// ── Lazy imports a nivel de módulo (referencias estables, evitan re-mount en Suspense) ──
const RouteItinerary  = lazy(() => import('./features/routes/components/RouteItinerary'));
const ClientForm      = lazy(() => import('./features/clients/components/ClientForm'));
const CustomerPortfolio = lazy(() => import('./features/clients/components/CustomerPortfolio'));
const CustomerMap     = lazy(() => import('./features/clients/components/CustomerMap'));
const SellerAudit     = lazy(() => import('./features/audit/components/SellerAudit'));
const EmployeeList    = lazy(() => import('./features/employee/components/EmployeeList'));

const drawerWidth = 260;

class ViewErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error: Error | null }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  override componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error("Detalle del fallo en el componente:", error, errorInfo);
  }

  override render() {
    if (this.state.hasError) {
      return (
        <div className="p-6 bg-red-950/40 border border-red-500/30 rounded-xl text-white my-4">
          <h2 className="text-lg font-bold text-red-400 mb-2">⚠ Error detectado en el Listado/Vista</h2>
          <p className="text-sm text-slate-300 font-mono bg-black/30 p-3 rounded mb-4">
            {this.state.error?.toString()}
          </p>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-bold rounded text-xs transition-colors"
          >
            Recargar Módulo
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

function AppContent() {
  const { isAuthenticated, user, logout, loading } = useAuth();
  const [themeMode, setThemeMode] = useState<ThemeMode>('dark');
  const [activeView, setActiveView] = useState<'agenda' | 'register' | 'portfolio' | 'audit' | 'employees' | 'map'>('agenda');
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileMenuAnchor, setProfileMenuAnchor] = useState<null | HTMLElement>(null);
  const [profileModalOpen, setProfileModalOpen] = useState(false);

  const toggleThemeMode = () => {
    setThemeMode((prevMode) => (prevMode === 'dark' ? 'light' : 'dark'));
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setProfileMenuAnchor(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setProfileMenuAnchor(null);
  };

  const handleOpenProfile = () => {
    handleProfileMenuClose();
    setProfileModalOpen(true);
  };

  React.useEffect(() => {
    if ((activeView === 'employees' || activeView === 'audit') && !user?.roles.includes('ADMIN') && !user?.roles.includes('ADMINISTRADOR')) {
      setActiveView('agenda');
    }
  }, [activeView, user]);

  // Mientras se verifica la cookie/sesión, mostrar spinner. Nunca mostrar Login si aún está cargando.
  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          gap: 2,
          backgroundColor: '#070f1a',
        }}
      >
        <CircularProgress sx={{ color: '#F29200' }} size={48} />
        <Typography sx={{ color: '#64748b', fontSize: '0.875rem' }}>
          Verificando sesión...
        </Typography>
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Login />;
  }

  const baseMenuItems = [
    { id: 'agenda', label: 'Agenda de Visitas', icon: '📅' },
    { id: 'portfolio', label: 'Cartera Clientes', icon: '👥' },
    { id: 'map', label: 'Mapa de Clientes', icon: '🗺️' },
    { id: 'employees', label: 'Empleados', icon: '💼' },
    { id: 'register', label: 'Registrar Cliente', icon: '📝' },
    { id: 'audit', label: 'Auditoría Vendedores', icon: '🛡️' },
  ] as const;

  const menuItems = baseMenuItems.filter(item => {
    if (item.id === 'employees' && !user?.roles.includes('ADMIN') && !user?.roles.includes('ADMINISTRADOR')) return false;
    if (item.id === 'audit' && !user?.roles.includes('ADMIN') && !user?.roles.includes('ADMINISTRADOR')) return false;
    return true;
  });

  const drawerContent = (
    <Box
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: themeMode === 'dark' ? '#0F1D33' : '#ffffff',
        color: themeMode === 'dark' ? '#ffffff' : '#0F1D33',
        borderRight: '1px solid',
        borderColor: 'divider'
      }}
    >
      {/* Header del Sidebar: Logo + Contenedor de Texto con Salto de Línea */}
      <Box
        sx={{
          p: 2.5,
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          borderBottom: '1px solid',
          borderColor: 'divider'
        }}
      >
        <img src={smlLogo} alt="Logo" className="h-[40px] w-auto object-contain flex-shrink-0" />
        <Box sx={{ display: 'flex', flexDirection: 'column', minWidth: 0, flexGrow: 1 }}>
          <Typography
            variant="subtitle1"
            sx={{
              fontWeight: 700,
              lineHeight: '1.2',
              letterSpacing: '0.5px',
              color: themeMode === 'dark' ? '#ffffff' : '#0F1D33',
              fontSize: '0.95rem',
              whiteSpace: 'normal',       // Permite múltiples líneas
              wordBreak: 'break-word',    // Rompe palabras largas limpiamente
              overflowWrap: 'anywhere'    // Respeta el ancho absoluto del contenedor
            }}
          >
            Panel de Control
          </Typography>
        </Box>
      </Box>

      {/* Lista de Navegación */}
      <List sx={{ flexGrow: 1, px: 2, py: 2, '& .MuiListItem-root': { mb: 0.5 } }}>
        {menuItems.map((item) => (
          <ListItem key={item.id} disablePadding>
            <ListItemButton
              onClick={() => {
                setActiveView(item.id);
                setMobileOpen(false);
              }}
              selected={activeView === item.id}
              sx={{
                borderRadius: 2,
                color: activeView === item.id ? '#F29200' : 'text.primary',
                '&.Mui-selected': {
                  backgroundColor: 'rgba(242, 146, 0, 0.12)',
                  color: '#F29200',
                  '& .MuiListItemIcon-root': { color: '#F29200' },
                  '&:hover': { backgroundColor: 'rgba(242, 146, 0, 0.18)' }
                },
                '&:hover': {
                  backgroundColor: themeMode === 'dark' ? 'rgba(255, 255, 255, 0.05)' : 'rgba(15, 29, 51, 0.05)'
                }
              }}
            >
              <ListItemIcon
                sx={{
                  minWidth: '36px',
                  fontSize: '1.2rem',
                  color: activeView === item.id ? '#F29200' : 'text.secondary'
                }}
              >
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                sx={{
                  '& .MuiListItemText-primary': {
                    fontSize: '0.875rem',
                    fontWeight: activeView === item.id ? 700 : 500,
                  }
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>

      <Divider sx={{ borderColor: 'divider' }} />

      {/* Botón de Cerrar Sesión */}
      <Box sx={{ p: 2 }}>
        <Button
          fullWidth
          variant="outlined"
          color="error"
          onClick={logout}
          sx={{
            borderRadius: 2,
            fontWeight: 'bold',
            textTransform: 'none',
            py: 1
          }}
        >
          Cerrar Sesión 🚪
        </Button>
      </Box>
    </Box>
  );

  return (
    <ThemeProvider theme={getTheme(themeMode)}>
      <CssBaseline />
      <div className="flex min-h-screen">
        <AppBar
          position="fixed"
          elevation={0}
          sx={{
            width: { md: `calc(100% - ${drawerWidth}px)` },
            ml: { md: `${drawerWidth}px` },
            backgroundColor: themeMode === 'dark' ? 'rgba(7, 15, 26, 0.8)' : 'rgba(244, 246, 249, 0.8)',
            backdropFilter: 'blur(12px)',
            borderBottom: '1px solid',
            borderColor: 'divider',
            backgroundImage: 'none'
          }}
        >
          <Toolbar className="justify-between px-4 md:px-8">
            <IconButton
              color="inherit"
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2, display: { md: 'none' }, color: 'text.primary' }}
            >
              ☰
            </IconButton>
            <Typography variant="h6" className="font-bold capitalize" sx={{ color: 'text.primary' }}>
              {menuItems.find(i => i.id === activeView)?.label}
            </Typography>

            <div className="flex items-center gap-4">
              <Tooltip title="Cambiar Tema">
                <IconButton onClick={toggleThemeMode} sx={{ color: 'text.primary', fontSize: '1.2rem' }}>
                  {themeMode === 'dark' ? '☀️' : '🌙'}
                </IconButton>
              </Tooltip>

              <Divider orientation="vertical" variant="middle" flexItem sx={{ my: 1.5 }} />

              <div className="flex items-center gap-3">
                <div className="hidden sm:block text-right">
                  <div className="text-sm font-bold" style={{ color: themeMode === 'dark' ? '#fff' : '#0F1D33' }}>
                    {user?.fullName || 'Cargando...'}
                  </div>
                  <div className="text-[10px] font-bold uppercase" style={{ color: '#64748b' }}>
                    {user?.code}
                  </div>
                </div>

                {/* Avatar interactivo */}
                <Tooltip title="Ver opciones de perfil">
                  <Box
                    id="user-profile-button"
                    aria-controls={profileMenuAnchor ? 'user-profile-menu' : undefined}
                    aria-haspopup="true"
                    aria-expanded={profileMenuAnchor ? 'true' : undefined}
                    onClick={handleProfileMenuOpen}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: 38,
                      height: 38,
                      borderRadius: '50%',
                      border: '2px solid',
                      borderColor: 'primary.main',
                      boxShadow: '0 2px 8px rgba(242, 146, 0, 0.2)',
                      backgroundColor: themeMode === 'dark' ? 'rgba(255, 255, 255, 0.05)' : 'rgba(15, 29, 51, 0.05)',
                      color: '#F29200',
                      cursor: 'pointer',
                      transition: 'box-shadow 0.2s, background-color 0.2s',
                      '&:hover': {
                        boxShadow: '0 4px 16px rgba(242, 146, 0, 0.4)',
                        backgroundColor: themeMode === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(15, 29, 51, 0.1)',
                      },
                    }}
                  >
                    <span
                      className="material-symbols-outlined"
                      style={{
                        fontSize: '28px',
                        fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 40"
                      }}
                    >
                      account_circle
                    </span>
                  </Box>
                </Tooltip>

                {/* Menú desplegable de perfil */}
                <Menu
                  id="user-profile-menu"
                  anchorEl={profileMenuAnchor}
                  open={Boolean(profileMenuAnchor)}
                  onClose={handleProfileMenuClose}
                  transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                  anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                  slotProps={{
                    list: {
                      'aria-labelledby': 'user-profile-button',
                    },
                    paper: {
                      elevation: 4,
                      sx: {
                        mt: 1,
                        borderRadius: 2,
                        minWidth: 180,
                        border: '1px solid',
                        borderColor: 'divider',
                      },
                    },
                  }}
                >
                  <MenuItem
                    id="menu-item-view-profile"
                    onClick={handleOpenProfile}
                    sx={{ gap: 1.5, fontWeight: 600, py: 1.25 }}
                  >
                    <SettingsIcon sx={{ fontSize: 20, color: '#F29200' }} />
                    Ver mi perfil
                  </MenuItem>
                </Menu>
              </div>
            </div>
          </Toolbar>
        </AppBar>

        {/* Modal de Perfil */}
        <UserProfileModal
          open={profileModalOpen}
          onClose={() => setProfileModalOpen(false)}
        />

        <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
          <Drawer
            variant="temporary"
            open={mobileOpen}
            onClose={handleDrawerToggle}
            ModalProps={{ keepMounted: true }}
            sx={{
              display: { xs: 'block', md: 'none' },
              '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
            }}
          >
            {drawerContent}
          </Drawer>
          <Drawer
            variant="permanent"
            sx={{
              display: { xs: 'none', md: 'block' },
              '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, border: 'none' },
            }}
            open
          >
            {drawerContent}
          </Drawer>
        </Box>

        <Box
          component="main"
          className="flex-grow p-4 md:p-8 mt-16 transition-all duration-300"
          sx={{
            width: { md: `calc(100% - ${drawerWidth}px)` },
            backgroundColor: 'background.default'
          }}
        >
          <Container maxWidth="xl" disableGutters>
            <Suspense fallback={<ModuleLoader />}>
              <ViewErrorBoundary>
                {activeView === 'agenda' && <RouteItinerary />}
                {activeView === 'portfolio' && <CustomerPortfolio />}
                {activeView === 'map' && <CustomerMap />}
                {activeView === 'employees' && <EmployeeList />}
                {activeView === 'register' && <ClientForm />}
                {activeView === 'audit' && <SellerAudit />}
              </ViewErrorBoundary>
            </Suspense>
          </Container>
        </Box>
      </div>
    </ThemeProvider>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <NotificationProvider>
        <AppContent />
      </NotificationProvider>
    </AuthProvider>
  );
}