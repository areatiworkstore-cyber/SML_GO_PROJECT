import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Button from '@mui/material/Button';
import Avatar from '@mui/material/Avatar';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import InputAdornment from '@mui/material/InputAdornment';
import Chip from '@mui/material/Chip';
import CircularProgress from '@mui/material/CircularProgress';
import Divider from '@mui/material/Divider';
import type { Client } from '../types';
import { clientService } from '../services';
import { useAuth } from '../../auth'; // 👈 Importamos el hook de autenticación para obtener el "/me"
import { MapButton } from '../../../components/MapButton';
import { useNotification } from '../../../context/NotificationContext';

export const ClientList: React.FC = () => {
  const { showError } = useNotification();
  const { user } = useAuth(); // 👈 Aquí tenemos los datos del usuario logueado actualmente (/users/me)
  const [clients, setClients] = useState<Client[]>([]);
  const [selectedPortfolio, setSelectedPortfolio] = useState<boolean>(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadClientsData();
  }, []);

  const loadClientsData = async () => {
    try {
      setLoading(true);
      // Llamamos al endpoint que mapea automáticamente los clientes del vendedor logueado
      const data = await clientService.getClients();
      setClients(data);
    } catch (error) {
      showError(error);
    } finally {
      setLoading(false);
    }
  };

  // Filtrado para la tabla de clientes adentro de la cartera
  const filteredClients = clients.filter(
    (c) =>
      c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.document_number.includes(searchTerm)
  );

  const getGiroName = (id: number) => {
    const giros: Record<number, string> = {
      1: 'LUBRICENTRO', 2: 'TALLER MECANICO', 3: 'FERRETERIA',
      4: 'INSTALACION ELECTRICA', 5: 'INSTALACION GASISTA',
      6: 'PLOMERIA', 8: 'OTRO',
    };
    return giros[id] || 'OTRO';
  };

  // Iniciales del Vendedor Logueado para el Avatar
  const sellerName = user?.fullName || 'Asesor Comercial';
  const initials = sellerName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  const sellerCode = user?.code || 'V-000';
  const sellerRole = user?.role || 'VENDEDOR';

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
        <CircularProgress color="primary" />
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 1, md: 3 } }}>
      {/* VISTA PRINCIPAL: Muestra la tarjeta del Asesor/Vendedor logueado */}
      {!selectedPortfolio ? (
        <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider', borderRadius: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2, mb: 3 }}>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                Cartera de Clientes por Asesor
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                Base de datos oficial de clientes y prospectos corporativos asignados a cada vendedor.
              </Typography>
            </Box>
          </Box>

          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 6, md: 4 }}>
              <Card sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3, bgcolor: 'background.paper', position: 'relative', overflow: 'visible' }}>
                <CardContent sx={{ pt: 3, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>

                  {/* Avatar con las iniciales del VENDEDOR real */}
                  <Avatar sx={{ bgcolor: 'info.main', width: 56, height: 56, mb: 1.5, fontWeight: 'bold' }}>
                    {initials}
                  </Avatar>

                  {/* Nombre del VENDEDOR real */}
                  <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 0.5, textAlign: 'center' }}>
                    {sellerName}
                  </Typography>

                  {/* Rol y Código del VENDEDOR real */}
                  <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 'bold', textTransform: 'uppercase', mb: 2 }}>
                    {sellerRole} • {sellerCode}
                  </Typography>

                  {/* Cantidad real de clientes asociados en el backend */}
                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1, px: 2, py: 0.5, bgcolor: 'rgba(242, 146, 0, 0.12)', borderRadius: 2, mb: 2, width: 'fit-content' }}>
                    <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                      {clients.length}
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                      CLIENTES
                    </Typography>
                  </Box>

                  <Divider sx={{ width: '100%', my: 1 }} />

                  <Button
                    size="small"
                    variant="text"
                    onClick={() => setSelectedPortfolio(true)}
                    sx={{ fontWeight: 'bold', textTransform: 'none', mt: 1 }}
                  >
                    Explorar Cartera &gt;
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Paper>
      ) : (
        /* VISTA DETALLE: Se activa al dar clic en Explorar Cartera */
        <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider', borderRadius: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2, mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Button variant="outlined" size="small" onClick={() => setSelectedPortfolio(false)}>
                ← Volver
              </Button>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 'bold' }}>
                  Cartera de: <span style={{ color: '#F29200' }}>{sellerName}</span>
                </Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                  Código: {sellerCode} — {clients.length} clientes asignados.
                </Typography>
              </Box>
            </Box>

            {/* Buscador operativo de clientes */}
            <TextField
              placeholder="Buscar por código, RUC/DNI o nombre..."
              variant="outlined"
              size="small"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              sx={{ width: { xs: '100%', sm: 300 }, ml: 'auto' }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                      </svg>
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Box>

          <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
            <Table sx={{ minWidth: 650 }}>
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 'bold' }}>Código</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Cliente / Razón Social</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>RUC / DNI</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Giro Comercial</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Dirección</TableCell>
                  <TableCell sx={{ fontWeight: 'bold', textAlign: 'center' }}>Ubicación GPS</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredClients.map((client) => (
                  <TableRow key={client.id} sx={{ '&:hover': { bgcolor: 'action.hover' } }}>
                    <TableCell>
                      <Chip label={client.code} size="small" color="primary" sx={{ fontWeight: 'bold' }} />
                    </TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>{client.name}</TableCell>
                    <TableCell>{client.document_number}</TableCell>
                    <TableCell>{getGiroName(client.business_type_id)}</TableCell>
                    <TableCell>{client.address}</TableCell>
                    <TableCell align="center">
                      <MapButton latitude={client.latitud} longitude={client.longitud} />
                    </TableCell>
                  </TableRow>
                ))}
                {filteredClients.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary', fontStyle: 'italic' }}>
                      No se encontraron clientes que coincidan con la búsqueda.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}
    </Box>
  );
};

export default ClientList;