import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import Grid from '@mui/material/Grid';
import Avatar from '@mui/material/Avatar';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardActions from '@mui/material/CardActions';
import CircularProgress from '@mui/material/CircularProgress';
import Chip from '@mui/material/Chip';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Divider from '@mui/material/Divider';
import { useNotification } from '../../../context/NotificationContext';
import { employeeService } from '../../employee/services';
import { geographicService } from '../../geographic/services';
import { apiClient } from '../../../services/api';
import type { ClientResponse, ClientUpdate, ClientCreate } from '../types';
import type { Employee } from '../../employee/types';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import { useAuth } from '../../auth';
import { ClientForm } from './ClientForm';

const DOC_TYPES: Record<number, string> = { 1: 'DNI', 2: 'RUC' };
const BIZ_TYPES: Record<number, string> = { 1: 'LUBRICENTRO', 2: 'TALLER MECANICO', 3: 'FERRETERIA', 4: 'INSTALACION ELECTRICA', 5: 'INSTALACION GASISTA', 6: 'PLOMERIA', 8: 'OTRO' };
const CLIENT_GROUPS: Record<number, string> = { 1: 'B2B', 2: 'B2C', 3: 'B2G', 4: 'C2C' };

interface AdvisorData {
  id: number;
  code: string;
  first_name: string;
  second_name: string | null;
  first_surname: string;
  second_surname: string | null;
  email: string;
  role?: string;
}

export const CustomerPortfolio: React.FC = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'ADMINISTRADOR';
  const { showSuccess, showError, showConfirm } = useNotification();
  const [searchTerm, setSearchTerm] = useState('');
  const [advisors, setAdvisors] = useState<AdvisorData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Estados para la sub-vista de la cartera detallada
  const [selectedAdvisor, setSelectedAdvisor] = useState<AdvisorData | null>(null);
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [loadingClients, setLoadingClients] = useState<boolean>(false);

  const [viewModalOpen, setViewModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [selectedClientData, setSelectedClientData] = useState<ClientResponse | null>(null);
  const [districtName, setDistrictName] = useState<string>('');

  useEffect(() => {
    if (viewModalOpen && selectedClientData?.district_id) {
       geographicService.getDistricts().then(districts => {
          const found = districts.find((d: any) => d.id === selectedClientData.district_id);
          if (found) setDistrictName(found.name);
       }).catch(() => setDistrictName('Desconocido'));
    }
  }, [viewModalOpen, selectedClientData]);

  useEffect(() => {
    if (isAdmin) {
      loadAdvisors();
    } else if (user) {
      const myAdvisorData: AdvisorData = {
        id: user.id,
        code: user.code,
        first_name: user.first_name,
        second_name: user.second_name || null,
        first_surname: user.first_surname,
        second_surname: user.second_surname || null,
        email: user.email || '',
        role: user.role
      };
      handleExplorePortfolio(myAdvisorData);
    }
  }, [isAdmin, user]);

  const loadAdvisors = async () => {
    try {
      setLoading(true);
      const data: Employee[] = await employeeService.getEmployees(0, 100);

      // Transformamos el tipo Employee al formato exacto de AdvisorData
      const formattedAdvisors: AdvisorData[] = (data || []).map((emp: any) => {
        // Determinamos el rol real basándonos en su código de empleado (SML001 es ADMIN en tu DB)
        // o si el backend llega a mandar alguna propiedad identificadora.
        const userCode = emp.code || '';
        const isSystemAdmin = userCode === 'SML001' || userCode.startsWith('ADM');

        return {
          id: emp.id,
          code: userCode || `ID-${emp.id}`,
          first_name: emp.first_name || '',
          second_name: emp.second_name || null,
          first_surname: emp.first_surname || '',
          second_surname: emp.second_surname || null,
          email: emp.email || '',
          role: isSystemAdmin ? 'ADMINISTRADOR' : 'VENDEDOR'
        };
      });

      setAdvisors(formattedAdvisors);
    } catch (error) {
      showError(error);
    } finally {
      setLoading(false);
    }
  };

  const handleExplorePortfolio = async (advisor: AdvisorData) => {
    try {
      setSelectedAdvisor(advisor);
      setLoadingClients(true);
      setClients([]);

      const clientData = await employeeService.getClientsByAdvisor(advisor.id);
      setClients(clientData || []);
    } catch (error) {
      showError("Error al cargar los clientes asignados.");
      setClients([
        { 
            id: 101, 
            code: 'CLI001', 
            name: 'Tecnologías del Sur S.A.C.', 
            document_number: '20601122334', 
            document_type_id: 2,
            cellphone: '951234567', 
            telephone: null,
            active: true, 
            user_id: advisor.id, 
            address: 'Av. El Sol 456', 
            district_id: 1,
            business_type_id: 1,
            client_group_id: 1,
            observation: 'Cliente Premium' 
        }
      ].filter(c => c.user_id === advisor.id) as ClientResponse[]);
    } finally {
      setLoadingClients(false);
    }
  };

  const handleBackToList = () => {
    setSelectedAdvisor(null);
    setClients([]);
  };

  const handleViewClient = async (client: ClientResponse) => {
    try {
      const response = await apiClient.get<ClientResponse>(`/clients/${client.id}`);
      setSelectedClientData(response);
      setViewModalOpen(true);
    } catch (error) {
      showError("Error al obtener los detalles del cliente.");
    }
  };

  const handleEditClient = async (client: ClientResponse) => {
    try {
      const response = await apiClient.get<ClientResponse>(`/clients/${client.id}`);
      setSelectedClientData(response);
      setEditModalOpen(true);
    } catch (error) {
      showError("Error al editar el cliente.");
    }
  };

  const closeModals = () => {
    setViewModalOpen(false);
    setEditModalOpen(false);
    setSelectedClientData(null);
    setDistrictName('');
  };

  const handleDeactivateClient = async (clientId: number) => {
    const confirmed = await showConfirm({
      title: 'Desactivar Cliente',
      message: '¿Está seguro de desactivar este cliente?'
    });
    if (confirmed) {
      try {
        await apiClient.delete(`/clients/${clientId}`);
        showSuccess('Cliente desactivado exitosamente.');
        if (selectedAdvisor) {
          handleExplorePortfolio(selectedAdvisor);
        }
      } catch (error) {
        showError("Error al desactivar el cliente.");
      }
    }
  };

  const buildFullName = (user: Pick<AdvisorData, 'first_name' | 'first_surname'>) => {
    const parts = [user.first_name, user.first_surname].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : 'Sin Nombre';
  };

  const getInitials = (firstName: string, firstSurname: string) => {
    return `${firstName?.[0] || ''}${firstSurname?.[0] || ''}`.toUpperCase();
  };

  const filteredAdvisors = advisors.filter(adv => {
    const fullName = `${adv.first_name} ${adv.first_surname}`.toLowerCase();
    const code = (adv.code || '').toLowerCase();
    const search = searchTerm.toLowerCase();
    return fullName.includes(search) || code.includes(search);
  });

  return (
    <Box sx={{ p: { xs: 1, md: 3 } }}>
      {/* CABECERA DINÁMICA */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
            {selectedAdvisor ? `💼 Cartera de: ${buildFullName(selectedAdvisor)}` : 'Cartera de Clientes por Trabajador'}
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            {selectedAdvisor
              ? `Visualizando los clientes corporativos vinculados al código de usuario ${selectedAdvisor.code}.`
              : 'Base de datos oficial de clientes asignados de forma exclusiva según el personal de ventas de SML Lubricantes.'
            }
          </Typography>
        </Box>
        {selectedAdvisor && isAdmin && (
          <Button variant="outlined" color="inherit" onClick={handleBackToList} sx={{ textTransform: 'none', fontWeight: 'bold' }}>
            ← Volver
          </Button>
        )}
      </Box>

      {/* SECCIÓN 1: VISTA GENERAL DE TARJETAS (ASESORES) */}
      {!selectedAdvisor && isAdmin && (
        <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider', borderRadius: 4, bgcolor: 'background.paper' }}>
          <Box sx={{ mb: 4, display: 'flex', justifyContent: 'flex-end' }}>
            <TextField
              placeholder="Buscar trabajador por nombre o código..."
              variant="outlined"
              size="small"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              sx={{ width: { xs: '100%', sm: 350 } }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                      </svg>
                    </InputAdornment>
                  ),
                }
              }}
            />
          </Box>

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
              <CircularProgress color="primary" />
            </Box>
          ) : (
            <Grid container spacing={3}>
              {filteredAdvisors.map((adv) => {
                const fullName = buildFullName(adv);
                // Usamos directamente el rol ya pre-calculado correctamente en el estado
                const determinedRole = adv.role;

                return (
                  <Grid key={adv.id} size={{ xs: 12, sm: 6, md: 4 }}>
                    <Card
                      elevation={0}
                      sx={{
                        border: '1px solid',
                        borderColor: 'divider',
                        borderRadius: 3,
                        transition: 'transform 0.2s, box-shadow 0.2s',
                        '&:hover': {
                          transform: 'translateY(-2px)',
                          boxShadow: '0 4px 20px rgba(0,0,0,0.25)',
                          borderColor: 'primary.main'
                        }
                      }}
                    >
                      <CardContent sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', pt: 4, pb: 1 }}>
                        <Avatar
                          sx={{
                            width: 64,
                            height: 64,
                            bgcolor: adv.role === 'ADMINISTRADOR' ? 'secondary.main' : 'info.main',
                            color: '#fff',
                            fontWeight: 'bold',
                            fontSize: '1.25rem',
                            mb: 2,
                            boxShadow: '0 0 0 4px rgba(2, 136, 209, 0.15)'
                          }}
                        >
                          {getInitials(adv.first_name, adv.first_surname)}
                        </Avatar>

                        <Typography variant="h6" sx={{ fontWeight: 'bold', textAlign: 'center', mb: 0.5 }}>
                          {fullName}
                        </Typography>

                        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1, textTransform: 'uppercase', fontSize: '0.72rem', letterSpacing: '1px' }}>
                          {determinedRole} • {adv.code}
                        </Typography>

                        <Typography variant="caption" sx={{ color: 'text.disabled', mb: 2 }}>
                          {adv.email}
                        </Typography>
                      </CardContent>

                      <CardActions sx={{ justifyContent: 'center', pb: 3, px: 2 }}>
                        <Button
                          size="small"
                          variant="text"
                          color="primary"
                          onClick={() => handleExplorePortfolio(adv)}
                          sx={{
                            textTransform: 'none',
                            fontWeight: 'bold',
                            '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' }
                          }}
                        >
                          Explarar Cartera &gt;
                        </Button>
                      </CardActions>
                    </Card>
                  </Grid>
                );
              })}

              {filteredAdvisors.length === 0 && (
                <Grid size={{ xs: 12 }}>
                  <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
                    <Typography variant="body1" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
                      No se encontraron trabajadores comerciales registrados en el sistema.
                    </Typography>
                  </Box>
                </Grid>
              )}
            </Grid>
          )}
        </Paper>
      )}

      {/* SECCIÓN 2: DETALLE DE LA CARTERA DEL ASESOR SELECCIONADO */}
      {selectedAdvisor && (
        <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider', borderRadius: 4, bgcolor: 'background.paper' }}>
          {loadingClients ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
              <CircularProgress color="primary" />
            </Box>
          ) : (
            <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
              <Table sx={{ minWidth: 650 }}>
                <TableHead sx={{ bgcolor: 'action.hover' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>Código Cliente</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Razón Social / Nombre</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>N° Documento</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Celular</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Dirección Fiscal</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Estado</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', textAlign: 'center' }}>Acciones</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {clients.map((cli) => (
                    <TableRow key={cli.id} sx={{ '&:hover': { bgcolor: 'action.hover' } }}>
                      <TableCell>
                        <Chip label={cli.code} size="small" variant="outlined" sx={{ fontWeight: 'bold' }} />
                      </TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{cli.name}</TableCell>
                      <TableCell>{cli.document_number}</TableCell>
                      <TableCell>{cli.cellphone || '---'}</TableCell>
                      <TableCell>{cli.address}</TableCell>
                      <TableCell>
                        <Chip
                          label={cli.active ? 'ASIGNADO' : 'INACTIVO'}
                          size="small"
                          sx={{
                            fontWeight: 'bold',
                            bgcolor: cli.active ? 'rgba(46, 125, 50, 0.12)' : 'rgba(211, 47, 47, 0.12)',
                            color: cli.active ? 'success.dark' : 'error.dark',
                          }}
                        />
                      </TableCell>
                      <TableCell align="center">
                          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1 }}>
                              <Tooltip title="Ver detalles">
                                  <IconButton size="small" onClick={() => handleViewClient(cli)}>👁️</IconButton>
                              </Tooltip>
                              <Tooltip title="Editar">
                                  <IconButton size="small" color="primary" onClick={() => handleEditClient(cli)}>✏️</IconButton>
                              </Tooltip>
                              <Tooltip title="Desactivar">
                                  <IconButton size="small" color="error" onClick={() => handleDeactivateClient(cli.id)}>🚫</IconButton>
                              </Tooltip>
                          </Box>
                      </TableCell>
                    </TableRow>
                  ))}

                  {clients.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ py: 5, color: 'text.secondary', fontStyle: 'italic' }}>
                        No existe ningún registro de clientes para la cartera de este vendedor.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Paper>
      )}

        <Dialog open={viewModalOpen} onClose={closeModals} maxWidth="md" fullWidth>
            <DialogTitle sx={{ fontWeight: 'bold', borderBottom: '1px solid', borderColor: 'divider' }}>
                📋 Información Completa del Cliente
            </DialogTitle>
            <DialogContent sx={{ p: 3 }}>
                {selectedClientData && (
                    <Grid container spacing={3}>
                        <Grid size={{ xs: 12 }}>
                            <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold', mb: 1 }}>Información General</Typography>
                            <Divider sx={{ mb: 2 }} />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Typography variant="body2" color="text.secondary">Código / Razón Social</Typography>
                            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>{selectedClientData.code} - {selectedClientData.name}</Typography>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Typography variant="body2" color="text.secondary">Clasificación Comercial</Typography>
                            <Typography variant="body1">{CLIENT_GROUPS[selectedClientData.client_group_id] || 'N/A'} • {BIZ_TYPES[selectedClientData.business_type_id] || 'N/A'}</Typography>
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                            <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold', mt: 2, mb: 1 }}>Información Fiscal / Contacto</Typography>
                            <Divider sx={{ mb: 2 }} />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Typography variant="body2" color="text.secondary">Documento</Typography>
                            <Typography variant="body1">{DOC_TYPES[selectedClientData.document_type_id] || 'Doc.'} {selectedClientData.document_number}</Typography>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Typography variant="body2" color="text.secondary">Celular / Teléfono</Typography>
                            <Typography variant="body1">{selectedClientData.cellphone || 'N/A'} {selectedClientData.telephone ? ` / ${selectedClientData.telephone}` : ''}</Typography>
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                            <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold', mt: 2, mb: 1 }}>Ubicación y Estado</Typography>
                            <Divider sx={{ mb: 2 }} />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Typography variant="body2" color="text.secondary">Dirección</Typography>
                            <Typography variant="body1">{selectedClientData.address} {districtName ? `(${districtName})` : ''}</Typography>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Typography variant="body2" color="text.secondary">Estado del Cliente</Typography>
                            <Chip 
                                label={selectedClientData.active ? 'ASIGNADO / ACTIVO' : 'INACTIVO'} 
                                size="small" 
                                sx={{ mt: 0.5, fontWeight: 'bold', bgcolor: selectedClientData.active ? 'rgba(46, 125, 50, 0.12)' : 'rgba(211, 47, 47, 0.12)', color: selectedClientData.active ? 'success.dark' : 'error.dark' }} 
                            />
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                            <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold', mt: 2, mb: 1 }}>Observaciones / Notas Internas</Typography>
                            <Divider sx={{ mb: 2 }} />
                            <Typography variant="body1" sx={{ fontStyle: selectedClientData.observation ? 'normal' : 'italic', color: selectedClientData.observation ? 'text.primary' : 'text.secondary' }}>
                                {selectedClientData.observation || 'No hay observaciones registradas para este cliente.'}
                            </Typography>
                        </Grid>
                    </Grid>
                )}
            </DialogContent>
            <DialogActions sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
                <Button onClick={closeModals} variant="outlined" color="inherit">Cerrar</Button>
            </DialogActions>
        </Dialog>

        <Dialog open={editModalOpen} onClose={closeModals} maxWidth="md" fullWidth>
            <DialogContent sx={{ p: 0 }}>
                {selectedClientData && (
                    <ClientForm 
                        initialData={selectedClientData as unknown as ClientCreate} 
                        clientId={selectedClientData.id}
                        onSubmitSuccess={() => { closeModals(); if (selectedAdvisor) handleExplorePortfolio(selectedAdvisor); }}
                        onCancel={closeModals}
                    />
                )}
            </DialogContent>
        </Dialog>
    </Box>
  );
};

export default CustomerPortfolio;