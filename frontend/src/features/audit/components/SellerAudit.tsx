import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import Card from '@mui/material/Card';
import Chip from '@mui/material/Chip';
import CircularProgress from '@mui/material/CircularProgress';
import Skeleton from '@mui/material/Skeleton';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import TextField from '@mui/material/TextField';
import { useTheme } from '@mui/material/styles';

import type { Client } from '../../clients/types';
import type { Route } from '../../routes/types';
import type { Employee } from '../../employee/types';
import type { ClientScheduleResponse } from '../../schedule_service/types';
import { clientService } from '../../clients/services/services';
import { routeService } from '../../routes/services';
import { employeeService } from '../../employee/services';
import { scheduleService } from '../../schedule_service';
import { MapButton } from '../../../components/MapButton';

const getBaseUrl = () => {
  const baseEnvUrl = import.meta.env.VITE_API_URL || '';
  if (baseEnvUrl.startsWith('http://') || baseEnvUrl.startsWith('https://')) {
    try {
      const url = new URL(baseEnvUrl);
      return url.origin;
    } catch {
      return '';
    }
  }
  return '';
};

export const SellerAudit: React.FC = () => {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  // Estados de Vendedores y Filtros
  const [sellers, setSellers] = useState<Employee[]>([]);
  const [loadingSellers, setLoadingSellers] = useState(true);
  const [sellersError, setSellersError] = useState<string | null>(null);
  const [searchCode, setSearchCode] = useState('');
  const [searchName, setSearchName] = useState('');
  const [selectedSellerId, setSelectedSellerId] = useState<number | ''>('');

  // Sección activa de auditoría
  const [activeTab, setActiveTab] = useState<'itineraries' | 'routes' | 'upcoming'>('itineraries');

  // Estados de datos de auditoría
  const [clients, setClients] = useState<Client[]>([]);
  const [routes, setRoutes] = useState<Route[]>([]);
  const [schedules, setSchedules] = useState<ClientScheduleResponse[]>([]);
  const [loading, setLoading] = useState(false);

  // Cargar vendedores al montar
  useEffect(() => {
    const loadSellers = async () => {
      setLoadingSellers(true);
      setSellersError(null);
      try {
        const data = await employeeService.getEmployees(0, 100);
        // Filtrar todos los trabajadores activos disponibles
        const activeSellers = (data || []).filter(
          emp => emp.active !== false
        );
        setSellers(activeSellers);
        if (activeSellers.length > 0) {
          setSelectedSellerId(activeSellers[0].id);
        }
      } catch (error) {
        console.error('Error cargando vendedores', error);
        setSellersError('No se pudo cargar la lista de trabajadores. Intente nuevamente.');
      } finally {
        setLoadingSellers(false);
      }
    };
    loadSellers();
  }, []);

  // Cargar datos del vendedor seleccionado
  useEffect(() => {
    if (selectedSellerId) {
      loadAuditData();
    }
  }, [selectedSellerId]);

  const loadAuditData = async () => {
    if (!selectedSellerId) return;
    setLoading(true);
    try {
      const [clientsData, routesData, schedulesData] = await Promise.all([
        clientService.getClients(selectedSellerId),
        routeService.getRoutes(selectedSellerId),
        scheduleService.getSchedules({ user_id: selectedSellerId }),
      ]);
      setClients(clientsData);
      setRoutes(routesData);
      setSchedules(schedulesData);
    } catch (error) {
      console.error('Error cargando información de auditoría', error);
    } finally {
      setLoading(false);
    }
  };

  // Filtrar vendedores dinámicamente
  const filteredSellers = sellers.filter(seller => {
    const matchesCode = (seller.code || '').toLowerCase().includes(searchCode.toLowerCase());
    const fullName = `${seller.first_name || ''} ${seller.last_name || ''}`.toLowerCase();
    const matchesName = fullName.includes(searchName.toLowerCase());
    return matchesCode && matchesName;
  });

  // Agrupar itinerarios/agendas por fecha
  const groupedSchedules = schedules.reduce((acc, s) => {
    const dateStr = s.day;
    if (!acc[dateStr]) acc[dateStr] = [];
    acc[dateStr].push(s);
    return acc;
  }, {} as Record<string, ClientScheduleResponse[]>);

  // Ordenar fechas descendente
  const sortedScheduleDates = Object.keys(groupedSchedules).sort((a, b) => b.localeCompare(a));

  return (
    <Box sx={{ p: { xs: 1, md: 3 } }}>
      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          backgroundColor: isDark ? '#0f1d33' : '#ffffff',
          border: '1px solid',
          borderColor: isDark ? 'rgba(242, 146, 0, 0.2)' : 'rgba(15, 29, 51, 0.08)',
          borderRadius: 4,
          boxShadow: isDark ? 'none' : '0 10px 30px rgba(15, 29, 51, 0.05)',
          mb: 4,
        }}
      >
        <Grid container spacing={3} sx={{ alignItems: 'center', mb: 3 }}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#F29200' }} gutterBottom>
              Panel de Auditoría y Supervisión de Rutas
            </Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500 }}>
              Controla las visitas de venta en tiempo real, verifica ubicaciones de clientes y audita el cumplimiento de la agenda de los vendedores.
            </Typography>
          </Grid>

          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>

              {/* Estado de error al cargar vendedores */}
              {sellersError && (
                <Alert severity="error" sx={{ borderRadius: 2 }}>
                  {sellersError}
                </Alert>
              )}

              {/* Filtros de búsqueda */}
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <TextField
                  label="Buscar por Código"
                  variant="outlined"
                  size="small"
                  value={searchCode}
                  onChange={(e) => setSearchCode(e.target.value)}
                  disabled={loadingSellers}
                  sx={{ flexGrow: 1, minWidth: 150 }}
                />
                <TextField
                  label="Buscar por Nombre"
                  variant="outlined"
                  size="small"
                  value={searchName}
                  onChange={(e) => setSearchName(e.target.value)}
                  disabled={loadingSellers}
                  sx={{ flexGrow: 1, minWidth: 150 }}
                />
              </Box>

              {/* Selector de vendedor con skeleton de carga */}
              {loadingSellers ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <CircularProgress size={18} thickness={5} color="primary" />
                    <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                      Cargando trabajadores...
                    </Typography>
                  </Box>
                  <Skeleton variant="rounded" height={40} sx={{ borderRadius: 1 }} />
                </Box>
              ) : (
                <FormControl fullWidth size="small">
                  <InputLabel id="seller-select-label">Seleccionar Vendedor</InputLabel>
                  <Select
                    labelId="seller-select-label"
                    value={selectedSellerId}
                    label="Seleccionar Vendedor"
                    onChange={(e) => setSelectedSellerId(Number(e.target.value))}
                    disabled={loadingSellers}
                  >
                    {filteredSellers.map(seller => (
                      <MenuItem key={seller.id} value={seller.id}>
                        {seller.code} - {seller.first_name} {seller.last_name}
                      </MenuItem>
                    ))}
                    {filteredSellers.length === 0 && !loadingSellers && (
                      <MenuItem disabled>
                        No se encontraron trabajadores.
                      </MenuItem>
                    )}
                  </Select>
                </FormControl>
              )}

            </Box>
          </Grid>
        </Grid>

        <Divider sx={{ my: 3, borderColor: 'divider' }} />

        {/* Sección de 3 opciones */}
        <Box sx={{ mb: 4 }}>
          <Tabs
            value={activeTab}
            onChange={(_, val) => setActiveTab(val)}
            indicatorColor="primary"
            textColor="primary"
            variant="scrollable"
            scrollButtons="auto"
          >
            <Tab label="Ver Itinerarios/Agendas" value="itineraries" sx={{ fontWeight: 'bold', textTransform: 'none' }} />
            <Tab label="Ver Rutas y Paradas" value="routes" sx={{ fontWeight: 'bold', textTransform: 'none' }} />
            <Tab label="Próximamente" value="upcoming" sx={{ fontWeight: 'bold', textTransform: 'none' }} />
          </Tabs>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '250px' }}>
            <CircularProgress color="primary" />
          </Box>
        ) : (
          <Box>
            {activeTab === 'itineraries' && (
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'text.primary', mb: 3 }}>
                  Historial de Itinerarios y Agendas
                </Typography>
                {sortedScheduleDates.length > 0 ? (
                  sortedScheduleDates.map((dateStr) => (
                    <Paper key={dateStr} variant="outlined" sx={{ p: 3, mb: 3, borderRadius: 3 }}>
                      <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: 'primary.main', mb: 2 }}>
                        📅 Fecha: {dateStr.split('-').reverse().join('/')}
                      </Typography>
                      <List disablePadding>
                        {groupedSchedules[dateStr]
                          .sort((a, b) => a.start_time.localeCompare(b.start_time))
                          .map((schedule) => {
                            const clientInfo = clients.find(c => c.id === schedule.client_id);
                            return (
                              <ListItem
                                key={schedule.id}
                                sx={{
                                  mb: 1.5,
                                  backgroundColor: isDark ? 'rgba(255, 255, 255, 0.02)' : '#fafafa',
                                  border: '1px solid',
                                  borderColor: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(15, 29, 51, 0.06)',
                                  borderRadius: 2,
                                  p: 2,
                                }}
                              >
                                <ListItemText
                                  primary={
                                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                                      {schedule.start_time.substring(0, 5)} Hrs - {clientInfo ? clientInfo.name : `Cliente ID: ${schedule.client_id}`}
                                    </Typography>
                                  }
                                  secondary={
                                    <Box sx={{ mt: 0.5 }}>
                                      <Typography variant="body2" color="text.secondary">
                                        📍 Dirección: {clientInfo?.address || 'No registrada'}
                                      </Typography>
                                      {schedule.observation && (
                                        <Typography variant="caption" sx={{ display: 'block', mt: 0.5, fontStyle: 'italic', color: 'text.secondary' }}>
                                          Memo: {schedule.observation}
                                        </Typography>
                                      )}
                                    </Box>
                                  }
                                />
                                <MapButton latitude={clientInfo?.latitud} longitude={clientInfo?.longitud} />
                              </ListItem>
                            );
                          })}
                      </List>
                    </Paper>
                  ))
                ) : (
                  <Typography variant="body2" sx={{ color: 'text.secondary', fontStyle: 'italic', textAlign: 'center', py: 4 }}>
                    No se encontraron itinerarios registrados para este vendedor.
                  </Typography>
                )}
              </Box>
            )}

            {activeTab === 'routes' && (
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'text.primary', mb: 3 }}>
                  Historial de Rutas y Paradas
                </Typography>
                {routes.length > 0 ? (
                  routes.map((route) => (
                    <Paper key={route.id} variant="outlined" sx={{ p: 3, mb: 3, borderRadius: 3 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', mb: 2, gap: 1 }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                          🔀 Ruta: {route.name}
                        </Typography>
                        <Chip label={`Fecha: ${String(route.scheduled_date).split('-').reverse().join('/')}`} size="small" variant="outlined" sx={{ fontWeight: 'bold' }} />
                      </Box>

                      <Divider sx={{ my: 1.5 }} />

                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
                        {route.waypoints && route.waypoints.length > 0 ? (
                          route.waypoints.map((wp) => {
                            const clientInfo = clients.find(c => c.id === wp.client_id);
                            const statusColor = wp.status === 'VISITA' ? 'success' : wp.status === 'CANCELADA' ? 'error' : 'default';
                            const statusLabel = wp.status === 'VISITA' ? 'Visitada' : wp.status === 'CANCELADA' ? 'Cancelada' : 'Pendiente';

                            return (
                              <Card key={wp.id} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, p: 2 }}>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', mb: 1, gap: 1 }}>
                                  <Box>
                                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                                      Parada #{wp.order_sequence}: {clientInfo ? clientInfo.name : `Cliente ID: ${wp.client_id}`}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                      📍 Dirección: {wp.address}
                                    </Typography>
                                  </Box>
                                  <Chip label={statusLabel} color={statusColor} size="small" sx={{ fontWeight: 'bold' }} />
                                </Box>

                                <Box sx={{ display: 'flex', gap: 1, mt: 1, flexWrap: 'wrap' }}>
                                  <Chip label={`Código Cliente: ${clientInfo?.code || 'N/A'}`} size="small" variant="outlined" sx={{ fontSize: '0.7rem' }} />
                                  <Chip label={`Doc Cliente: ${clientInfo?.document_number || 'N/A'}`} size="small" variant="outlined" sx={{ fontSize: '0.7rem' }} />
                                  {clientInfo?.cellphone && <Chip label={`Teléfono: ${clientInfo.cellphone}`} size="small" variant="outlined" sx={{ fontSize: '0.7rem' }} />}
                                </Box>

                                {wp.comment && (
                                  <Box sx={{ mt: 1.5, p: 1.5, bgcolor: 'action.hover', borderRadius: 1.5, borderLeft: '3px solid', borderColor: 'primary.main' }}>
                                    <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block', mb: 0.5 }}>Comentario:</Typography>
                                    <Typography variant="body2" sx={{ fontStyle: 'italic' }}>{wp.comment}</Typography>
                                  </Box>
                                )}

                                {wp.url_photo && (
                                  <Box sx={{ mt: 1.5 }}>
                                    <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block', mb: 0.5 }}>Foto de evidencia:</Typography>
                                    <Box
                                      component="img"
                                      src={`${getBaseUrl()}${wp.url_photo}`}
                                      alt="Evidencia fotográfica"
                                      sx={{ maxWidth: 200, maxHeight: 200, borderRadius: 2, objectFit: 'cover', cursor: 'pointer', border: '1px solid', borderColor: 'divider' }}
                                      onClick={() => window.open(`${getBaseUrl()}${wp.url_photo}`, '_blank')}
                                    />
                                  </Box>
                                )}
                              </Card>
                            );
                          })
                        ) : (
                          <Typography variant="body2" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
                            Esta ruta no contiene paradas.
                          </Typography>
                        )}
                      </Box>
                    </Paper>
                  ))
                ) : (
                  <Typography variant="body2" sx={{ color: 'text.secondary', fontStyle: 'italic', textAlign: 'center', py: 4 }}>
                    No se encontraron rutas registradas para este vendedor.
                  </Typography>
                )}
              </Box>
            )}

            {activeTab === 'upcoming' && (
              <Box sx={{ py: 6, textAlign: 'center' }}>
                <Typography variant="h6" color="text.secondary">
                  Esta funcionalidad estará disponible próximamente 🚀
                </Typography>
              </Box>
            )}
          </Box>
        )}
      </Paper>
    </Box>
  );
};

export default SellerAudit;