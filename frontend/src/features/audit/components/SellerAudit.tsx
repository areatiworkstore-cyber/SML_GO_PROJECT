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
import CardContent from '@mui/material/CardContent';
import Chip from '@mui/material/Chip';
import LinearProgress from '@mui/material/LinearProgress';
import CircularProgress from '@mui/material/CircularProgress';
import Divider from '@mui/material/Divider';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import { useTheme } from '@mui/material/styles';
import type { Client } from '../../clients/types';
import type { Route } from '../../routes/types';
import { clientService } from '../../clients/services/services';
import { routeService } from '../../routes/services';
import { MapButton } from '../../../components/MapButton';

export const SellerAudit: React.FC = () => {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  const [selectedSellerId, setSelectedSellerId] = useState<number>(1);
  const [clients, setClients] = useState<Client[]>([]);
  const [routes, setRoutes] = useState<Route[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadAuditData();
  }, [selectedSellerId]);

  const loadAuditData = async () => {
    setLoading(true);
    try {
      const [clientsData, routesData] = await Promise.all([
        clientService.getClients(selectedSellerId),
        routeService.getRoutes(selectedSellerId),
      ]);
      setClients(clientsData);
      setRoutes(routesData);
    } catch (error) {
      console.error('Error cargando información de auditoría', error);
    } finally {
      setLoading(false);
    }
  };

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
        <Grid container spacing={3} sx={{ alignItems: 'center' }}>
          <Grid size={{ xs: 12, sm: 8 }}>
            <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#F29200' }} gutterBottom>
              Panel de Auditoría y Supervisión de Rutas
            </Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500 }}>
              Controla las visitas de venta en tiempo real, verifica ubicaciones de clientes y audita el cumplimiento de la agenda de los vendedores.
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <FormControl fullWidth size="small">
              <InputLabel id="seller-select-label">Seleccionar Vendedor</InputLabel>
              <Select
                labelId="seller-select-label"
                value={selectedSellerId}
                label="Seleccionar Vendedor"
                onChange={(e) => setSelectedSellerId(Number(e.target.value))}
              >
                <MenuItem value={1}>VEN001 - Juan Carlos Perez</MenuItem>
                <MenuItem value={2}>VEN002 - Maria Fernandez</MenuItem>
                <MenuItem value={3}>VEN003 - Roberto Gomez</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>

        <Divider sx={{ my: 3, borderColor: 'divider' }} />

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '250px' }}>
            <CircularProgress color="primary" />
          </Box>
        ) : (
          <Grid container spacing={4}>
            {/* Column 1: Assigned Clients */}
            <Grid size={{ xs: 12, lg: 6 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                  Clientes Asignados
                </Typography>
                <Chip
                  label={`${clients.length} Clientes`}
                  size="small"
                  sx={{
                    bgcolor: 'rgba(242, 146, 0, 0.1)',
                    color: '#F29200',
                    borderColor: '#F29200',
                    border: '1px solid',
                    fontWeight: 'bold'
                  }}
                />
              </Box>

              <Box sx={{ maxHeight: 400, overflowY: 'auto', pr: 1 }}>
                <List disablePadding>
                  {clients.map((client) => (
                    <ListItem
                      key={client.id}
                      sx={{
                        mb: 2,
                        backgroundColor: isDark ? 'rgba(255, 255, 255, 0.02)' : '#fafafa',
                        border: '1px solid',
                        borderColor: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(15, 29, 51, 0.06)',
                        borderRadius: 2,
                        p: 2,
                      }}
                      secondaryAction={
                        <MapButton latitude={client.latitud} longitude={client.longitud} />
                      }
                    >
                      <ListItemText
                        primary={
                          <Typography variant="subtitle2" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                            {client.name}
                          </Typography>
                        }
                        secondary={
                          <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                            Código: {client.code} | Dirección: {client.address}
                          </Typography>
                        }
                      />
                    </ListItem>
                  ))}
                  {clients.length === 0 && (
                    <Typography variant="body2" sx={{ color: 'text.secondary', fontStyle: 'italic', textAlign: 'center', py: 4 }}>
                      No hay clientes asignados a este vendedor.
                    </Typography>
                  )}
                </List>
              </Box>
            </Grid>

            {/* Column 2: Visitas & Rutas (Compliance) */}
            <Grid size={{ xs: 12, lg: 6 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                  Cumplimiento de Agendas
                </Typography>
                <Chip
                  label={`${routes.length} Rutas`}
                  size="small"
                  sx={{
                    bgcolor: 'rgba(242, 146, 0, 0.1)',
                    color: '#F29200',
                    borderColor: '#F29200',
                    border: '1px solid',
                    fontWeight: 'bold'
                  }}
                />
              </Box>

              <Box sx={{ maxHeight: 400, overflowY: 'auto', pr: 1 }}>
                {routes.map((route) => {
                  const visitedCount = route.waypoints.filter((w) => w.status === 'VISITA').length;
                  const canceledCount = route.waypoints.filter((w) => w.status === 'CANCELADA').length;
                  const totalStops = route.waypoints.length;
                  const progressValue = totalStops > 0 ? ((visitedCount + canceledCount) / totalStops) * 100 : 0;

                  return (
                    <Card
                      key={route.id}
                      elevation={0}
                      sx={{
                        mb: 2,
                        backgroundColor: isDark ? 'rgba(255, 255, 255, 0.02)' : '#fafafa',
                        border: '1px solid',
                        borderColor: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(15, 29, 51, 0.06)',
                        borderRadius: 3,
                      }}
                    >
                      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                          <Box>
                            <Typography variant="subtitle2" sx={{ fontWeight: 'bold', color: '#F29200' }}>
                              {route.name}
                            </Typography>
                            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                              📅 {route.scheduled_date}
                            </Typography>
                          </Box>
                          <Box sx={{ textAlign: 'right' }}>
                            <Typography variant="subtitle2" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                              {visitedCount + canceledCount} / {totalStops} Paradas
                            </Typography>
                            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontWeight: 500 }}>
                              Visitas: {visitedCount} | Canceladas: {canceledCount}
                            </Typography>
                          </Box>
                        </Box>
                        <Box sx={{ width: '100%', mt: 2 }}>
                          <LinearProgress
                            variant="determinate"
                            value={progressValue}
                            sx={{
                              height: 6,
                              borderRadius: 3,
                              backgroundColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(15, 29, 51, 0.08)',
                              '& .MuiLinearProgress-bar': {
                                backgroundColor: progressValue === 100 ? '#4caf50' : '#F29200',
                                borderRadius: 3,
                              },
                            }}
                          />
                        </Box>
                      </CardContent>
                    </Card>
                  );
                })}
                {routes.length === 0 && (
                  <Typography variant="body2" sx={{ color: 'text.secondary', fontStyle: 'italic', textAlign: 'center', py: 4 }}>
                    No hay rutas agendadas para este vendedor.
                  </Typography>
                )}
              </Box>
            </Grid>
          </Grid>
        )}
      </Paper>
    </Box>
  );
};

export default SellerAudit;