import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import CircularProgress from '@mui/material/CircularProgress';
import Card from '@mui/material/Card';
import Tooltip from '@mui/material/Tooltip';
import IconButton from '@mui/material/IconButton';
import Divider from '@mui/material/Divider';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Grid from '@mui/material/Grid';

import { useNotification } from '../../../context/NotificationContext';
import { MapButton } from '../../../components/MapButton';
import { ScheduleModal } from '../../schedule_service/components/scheduleModal';
import { CreateRouteModal } from './CreateRouteModal';
import type { ClientScheduleResponse } from '../../schedule_service/types';
import type { Client, BusinessType, ClientGroup } from '../../clients/types';
import { scheduleService } from '../../schedule_service';
import { clientService, masterDataService } from '../../clients';
import { geographicService } from '../../geographic/services';
import type { DepartmentResponse, ProvinceResponse, DistrictResponse } from '../../geographic/types';

// Helper para calcular las fechas reales de la semana actual (Lunes a Sábado) dinámicamente
const getWeekDates = () => {
  const current = new Date();
  const day = current.getDay();
  const distanceToMonday = day === 0 ? -6 : 1 - day;

  const monday = new Date(current);
  monday.setDate(current.getDate() + distanceToMonday);

  const daysNames = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
  const mapping: { [key: string]: string } = {};

  daysNames.forEach((name, index) => {
    const targetDate = new Date(monday);
    targetDate.setDate(monday.getDate() + index);
    const year = targetDate.getFullYear();
    const month = String(targetDate.getMonth() + 1).padStart(2, '0');
    const dateStr = String(targetDate.getDate()).padStart(2, '0');
    mapping[name] = `${year}-${month}-${dateStr}`;
  });

  return mapping;
};

export const RouteItinerary: React.FC = () => {
  const { showConfirm, showSuccess, showError } = useNotification();

  // Estados de control de datos
  const [schedules, setSchedules] = useState<ClientScheduleResponse[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  // Estados para Almacenar Datos Maestros de Relaciones
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [provinces, setProvinces] = useState<ProvinceResponse[]>([]);
  const [districts, setDistricts] = useState<DistrictResponse[]>([]);
  const [businessTypes, setBusinessTypes] = useState<BusinessType[]>([]);
  const [clientGroups, setClientGroups] = useState<ClientGroup[]>([]);

  // 🚀 Estado de apertura para el Modal de asignación
  const [openModal, setOpenModal] = useState<boolean>(false);
  const [openRouteModal, setOpenRouteModal] = useState<boolean>(false);

  // 🗓️ CONTROL DE DIAS DINÁMICOS (Lunes a Sábado sin "Hoy")
  const weekDatesMapping = React.useMemo(() => getWeekDates(), []);
  const daysOfWeek = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];

  const getInitialDay = () => {
    const currentDayIndex = new Date().getDay(); // 0: Dom, 1: Lun, ..., 6: Sáb
    if (currentDayIndex === 0 || currentDayIndex === 6) return 'Sábado';
    return daysOfWeek[currentDayIndex - 1];
  };

  const [activeTab, setActiveTab] = useState<string>(getInitialDay());
  const [targetDateStr, setTargetDateStr] = useState<string>(weekDatesMapping[getInitialDay()]);

  // 📝 ESTADO DE LA PARADA SELECCIONADA PARA EL PANEL DERECHO
  const [selectedScheduleId, setSelectedScheduleId] = useState<number | null>(null);

  // Carga de datos inicial corregida para poblar el estado de clientes
  // Carga de datos inicial corregida para poblar el estado de clientes y maestros
  const loadData = async () => {
    try {
      setLoading(true);

      // Ejecutamos todas las peticiones en paralelo (Itinerario, Clientes, Ubigeos y Maestras)
      const [
        schedulesData,
        clientsData,
        departmentsData,
        provincesData,
        districtsData,
        businessTypesData,
        clientGroupsData
      ] = await Promise.all([
        scheduleService.getSchedules(),
        clientService.getClients(),
        // 🔄 Inyecciones de llamadas concurrentes a tus servicios del backend
        geographicService.getDepartments ? geographicService.getDepartments() : Promise.resolve([]),
        geographicService.getProvinces ? geographicService.getProvinces() : Promise.resolve([]),
        geographicService.getDistricts ? geographicService.getDistricts() : Promise.resolve([]),
        masterDataService.getBusinessTypes ? masterDataService.getBusinessTypes() : Promise.resolve([]),
        masterDataService.getClientGroups ? masterDataService.getClientGroups() : Promise.resolve([])
      ]);

      setSchedules(schedulesData);
      setClients(clientsData);
      setDepartments(departmentsData);
      setProvinces(provincesData);
      setDistricts(districtsData);
      setBusinessTypes(businessTypesData);
      setClientGroups(clientGroupsData);
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // Filtrar y ordenar las programaciones del día seleccionado (fecha ISO dinámica)
  const sortedSchedules = [...schedules]
    .filter(s => s.active && s.day === targetDateStr)
    .sort((a, b) => a.start_time.localeCompare(b.start_time));

  // 🔄 EFECTO: Selecciona automáticamente la primera parada al cambiar de día
  useEffect(() => {
    if (sortedSchedules.length > 0) {
      setSelectedScheduleId(sortedSchedules[0].id);
    } else {
      setSelectedScheduleId(null);
    }
  }, [schedules, targetDateStr]);

  // Manejador del cambio de pestaña de días
  const handleTabChange = (_event: React.SyntheticEvent, newValue: string) => {
    setActiveTab(newValue);
    setTargetDateStr(weekDatesMapping[newValue]);
  };

  // 🗑️ ELIMINAR PARADA OPTIMIZADA CON EL MODAL GLOBAL
  const handleDeleteSchedule = async (scheduleId: number) => {
    const confirmed = await showConfirm({
      title: 'Eliminar parada',
      message: '¿Está seguro de que desea eliminar esta parada del itinerario? Esta acción no se puede deshacer.',
    });

    if (!confirmed) return;

    try {
      setLoading(true);
      await scheduleService.deleteSchedule(scheduleId);
      showSuccess('La parada ha sido eliminada del itinerario correctamente.');
      await loadData();
    } catch (err: any) {
      showError(err);
    } finally {
      setLoading(false);
    }
  };

  // Variables calculadas para el Panel Derecho interactivo
  const activeScheduleData = sortedSchedules.find(s => s.id === selectedScheduleId);
  const activeClientData = activeScheduleData ? clients.find(c => c.id === activeScheduleData.client_id) : null;

  // 🔍 Funciones resolvedoras para mapear los IDs correspondientes a strings descriptivos
  const getClientLocationText = () => {
    if (!activeClientData || !activeClientData.district_id) return 'No registrada';

    const districtObj = districts.find(d => d.id === activeClientData.district_id);
    if (!districtObj) return `Distrito ID: ${activeClientData.district_id}`;

    const provinceObj = provinces.find(p => p.id === districtObj.province_id);
    const departmentObj = provinceObj ? departments.find(dep => dep.id === provinceObj.department_id) : null;

    const departmentName = departmentObj ? departmentObj.name : '';
    const provinceName = provinceObj ? provinceObj.name : '';
    const districtName = districtObj.name;

    return `${departmentName}${provinceName ? `, ${provinceName}` : ''} - ${districtName}`;
  };

  const getBusinessTypeText = () => {
    if (!activeClientData || !activeClientData.business_type_id) return 'No registrado';
    const typeObj = businessTypes.find(b => b.id === activeClientData.business_type_id);
    return typeObj ? typeObj.description : `ID: ${activeClientData.business_type_id}`;
  };

  const getClientGroupText = () => {
    if (!activeClientData || !activeClientData.client_group_id) return 'Sin grupo asignado';
    const groupObj = clientGroups.find(g => g.id === activeClientData.client_group_id);
    return groupObj ? groupObj.description : `Grupo ID: ${activeClientData.client_group_id}`;
  };

  return (
    <Paper
      elevation={0}
      sx={{
        p: { xs: 2, md: 4 },
        borderRadius: 4,
        border: '1px solid',
        borderColor: 'divider',
        backgroundColor: 'background.paper',
      }}
    >
      {/* HEADER DE LA SECCIÓN */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <div>
          <Typography variant="h5" sx={{ fontWeight: 'bold', color: 'text.primary', display: 'flex', alignItems: 'center', gap: 1.5 }}>
            Itinerario del {activeTab.toLowerCase()} ({targetDateStr.split('-').reverse().join('/')})
            <Chip
              label={`${sortedSchedules.length} visitas`}
              size="small"
              sx={{ bgcolor: 'rgba(242, 146, 0, 0.1)', color: 'primary.main', fontWeight: 'bold' }}
            />
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
            Mapeo de visitas comerciales configurado para la fecha indicada.
          </Typography>
        </div>

        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            color="primary"
            onClick={() => setOpenRouteModal(true)}
            sx={{ fontWeight: 'bold', px: 3, py: 1, borderRadius: 2 }}
          >
            + Crear Ruta
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={() => setOpenModal(true)}
            sx={{ fontWeight: 'bold', px: 3, py: 1, borderRadius: 2, color: 'secondary.main' }}
          >
            + Programar Visita
          </Button>
        </Box>
      </Box>

      {/* 🗓️ BARRA DE NAVEGACIÓN POR DÍAS (SÓLO LUNES A SÁBADO) */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 4 }}>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          textColor="primary"
          indicatorColor="primary"
        >
          {daysOfWeek.map((day) => (
            <Tab
              key={day}
              label={day}
              value={day}
              sx={{ fontWeight: 'bold', textTransform: 'none', minWidth: 100 }}
            />
          ))}
        </Tabs>
      </Box>

      {loading && schedules.length === 0 ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress color="primary" />
        </Box>
      ) : (
        <Grid container spacing={3}>

          {/* 🕒 TIMELINE AUTOMÁTICO (PANEL IZQUIERDO) */}
          <Grid size={{ xs: 12, md: 7 }}>
            <Box sx={{
              position: 'relative',
              pl: { xs: 2, sm: 4 },
              borderLeft: '2px solid',
              borderColor: 'divider',
              display: 'flex',
              flexDirection: 'column',
              gap: 3,
              py: 1
            }}>
              {sortedSchedules.length > 0 ? (
                sortedSchedules.map((schedule, index) => {
                  const clientInfo = clients.find((c) => c.id === schedule.client_id);
                  const formattedHour = schedule.start_time.substring(0, 5);
                  const isSelected = selectedScheduleId === schedule.id;

                  return (
                    <Box
                      key={schedule.id}
                      sx={{
                        position: 'relative',
                        display: 'flex',
                        flexDirection: { xs: 'column', sm: 'row' },
                        alignItems: { xs: 'flex-start', sm: 'center' },
                        gap: 2
                      }}
                    >
                      <Box sx={{
                        position: 'absolute',
                        left: { xs: '-25px', sm: '-41px' },
                        top: { xs: '8px', sm: '50%' },
                        transform: 'translateY(-50%)',
                        width: 16,
                        height: 16,
                        borderRadius: '50%',
                        bgcolor: isSelected ? 'primary.main' : 'action.disabled',
                        border: '4px solid',
                        borderColor: 'background.paper',
                        boxShadow: isSelected ? '0 0 0 2px #F29200' : '0 0 0 2px rgba(0,0,0,0.1)',
                        zIndex: 2,
                        transition: 'all 0.2s'
                      }} />

                      <Box sx={{ minWidth: 70 }}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold', color: isSelected ? 'primary.main' : 'text.secondary', lineHeight: 1 }}>
                          {formattedHour}
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.disabled', fontWeight: 'bold' }}>
                          PARADA #{index + 1}
                        </Typography>
                      </Box>

                      <Card
                        elevation={0}
                        onClick={() => setSelectedScheduleId(schedule.id)}
                        sx={{
                          flexGrow: 1,
                          width: '100%',
                          cursor: 'pointer',
                          border: '1px solid',
                          borderColor: isSelected ? 'primary.main' : 'divider',
                          borderRadius: 3,
                          p: 2,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          gap: 2,
                          backgroundColor: isSelected ? 'rgba(242, 146, 0, 0.01)' : 'background.paper',
                          transition: 'all 0.2s',
                          '&:hover': {
                            borderColor: 'primary.main',
                            boxShadow: '0 4px 20px rgba(0,0,0,0.04)',
                            transform: 'translateY(-2px)'
                          },
                        }}
                      >
                        <Box sx={{ minWidth: 0 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                            {clientInfo ? clientInfo.name : `Cliente ID: ${schedule.client_id}`}
                          </Typography>
                          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            📍 {clientInfo?.address || 'Dirección no registrada'}
                          </Typography>
                          <Box sx={{ display: 'flex', gap: 1, mt: 1, flexWrap: 'wrap' }}>
                            <Chip label={`Código: ${clientInfo?.code || 'N/A'}`} size="small" variant="outlined" sx={{ fontSize: '0.7rem', height: 20 }} />
                            <Chip label={`Doc: ${clientInfo?.document_number || 'N/A'}`} size="small" variant="outlined" sx={{ fontSize: '0.7rem', height: 20 }} />
                          </Box>
                        </Box>

                        <Box onClick={(e) => e.stopPropagation()} sx={{ flexShrink: 0, display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Tooltip title="Eliminar parada">
                            <IconButton
                              size="small"
                              color="error"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDeleteSchedule(schedule.id);
                              }}
                              sx={{
                                border: '1px solid',
                                borderColor: 'error.light',
                                borderRadius: 2,
                                p: 0.5,
                                '&:hover': { bgcolor: 'error.shortest' }
                              }}
                            >
                              🗑️
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </Card>
                    </Box>
                  );
                })
              ) : (
                <Box sx={{ p: 6, textAlign: 'center', border: '2px dashed', borderColor: 'divider', borderRadius: 4, bgcolor: 'action.hover' }}>
                  <Typography variant="body1" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                    No hay paradas agendadas para esta fecha
                  </Typography>
                </Box>
              )}
            </Box>
          </Grid>

          {/* 📝 PANEL DERECHO (DETALLES Y TAREAS MODIFICADO) */}
          <Grid size={{ xs: 12, md: 5 }}>
            <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 4, p: 3, height: '100%', display: 'flex', flexDirection: 'column', backgroundColor: 'background.paper' }}>
              {activeScheduleData && activeClientData ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 2.5 }}>
                  <div>
                    <Typography variant="caption" sx={{ color: 'primary.main', fontWeight: 'bold', display: 'block', mb: 0.5, textTransform: 'uppercase' }}>
                      Detalles de la Visita — {activeScheduleData.start_time.substring(0, 5)} Hrs
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'text.primary', lineHeight: 1.2 }}>
                      {activeClientData.name}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1, display: 'flex', alignItems: 'start', gap: 0.5 }}>
                      📍 {activeClientData.address || 'Sin dirección registrada'}
                    </Typography>
                  </div>

                  <Divider />

                  {/* 📝 SECCIÓN: Observación de Gestión Comercial */}
                  <Box>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                      📝 Observación de Gestión Comercial
                    </Typography>
                    <Box sx={{ p: 2, bgcolor: 'action.hover', borderRadius: 2.5, border: '1px solid', borderColor: 'divider' }}>
                      <Typography variant="body2" color={activeScheduleData.observation?.trim() ? "text.primary" : "text.secondary"} sx={{ fontStyle: activeScheduleData.observation?.trim() ? 'normal' : 'italic' }}>
                        {activeScheduleData.observation?.trim() ? activeScheduleData.observation : 'No hay ninguna observacion'}
                      </Typography>
                    </Box>
                  </Box>

                  {/* 👤 SECCIÓN: Datos del Cliente con IDs Resueltos Relacionalmente */}
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                      👤 Datos del Cliente
                    </Typography>

                    <Grid container spacing={2}>
                      <Grid size={{ xs: 6 }}>
                        <Typography variant="caption" color="text.disabled" sx={{ fontWeight: 'bold', display: 'block' }}>CELULAR</Typography>
                        <Typography variant="body2" color="text.primary" sx={{ fontWeight: 500 }}>{activeClientData.cellphone || 'No registrado'}</Typography>
                      </Grid>

                      <Grid size={{ xs: 6 }}>
                        <Typography variant="caption" color="text.disabled" sx={{ fontWeight: 'bold', display: 'block' }}>TIPO DE NEGOCIO</Typography>
                        <Typography variant="body2" color="text.primary" sx={{ fontWeight: 500 }}>{getBusinessTypeText()}</Typography>
                      </Grid>

                      <Grid size={{ xs: 12 }}>
                        <Typography variant="caption" color="text.disabled" sx={{ fontWeight: 'bold', display: 'block' }}>UBICACIÓN</Typography>
                        <Typography variant="body2" color="text.primary" sx={{ fontWeight: 500 }}>
                          {getClientLocationText()}
                        </Typography>
                      </Grid>

                      <Grid size={{ xs: 12 }}>
                        <Typography variant="caption" color="text.disabled" sx={{ fontWeight: 'bold', display: 'block' }}>GRUPO DE CLIENTE</Typography>
                        <Typography variant="body2" color="text.primary" sx={{ fontWeight: 500 }}>{getClientGroupText()}</Typography>
                      </Grid>

                      <Grid size={{ xs: 12 }}>
                        <Typography variant="caption" color="text.disabled" sx={{ fontWeight: 'bold', display: 'block' }}>OBSERVACIÓN DEL CLIENTE</Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ fontStyle: activeClientData.observation ? 'normal' : 'italic' }}>
                          {activeClientData.observation || 'Sin observaciones de perfil'}
                        </Typography>
                      </Grid>
                    </Grid>
                  </Box>

                  <Box sx={{ pt: 2, borderTop: '1px solid', borderColor: 'divider', display: 'flex', gap: 1.5 }}>
                    <Box sx={{ width: '100%' }}>
                      <MapButton latitude={activeClientData.latitud} longitude={activeClientData.longitud} label="Navegar GPS" size="medium" variant="contained" />
                    </Box>
                    <Button
                      variant="outlined"
                      color="inherit"
                      onClick={() => {
                        if (activeClientData.cellphone) {
                          window.location.href = `tel:${activeClientData.cellphone}`;
                        }
                      }}
                      sx={{ borderRadius: 2, px: 2, borderColor: 'divider' }}
                    >
                      📞 Llamar
                    </Button>
                  </Box>
                </Box>
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flexGrow: 1, gap: 1.5, py: 6, textAlign: 'center' }}>
                  <Box sx={{ fontSize: '2.5rem' }}>🎯</Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 'bold', color: 'text.secondary' }}>
                    Ninguna parada seleccionada
                  </Typography>
                </Box>
              )}
            </Box>
          </Grid>

        </Grid>
      )}

      {/* 🚀 MODAL TOTALMENTE INTEGRADO Y ACTIVO */}
      <ScheduleModal
        open={openModal}
        onClose={() => {
          setOpenModal(false);
          loadData();
        }}
        initialDate={targetDateStr}
      />

      <CreateRouteModal
        open={openRouteModal}
        onClose={() => {
          setOpenRouteModal(false);
          loadData();
        }}
        initialDate={targetDateStr}
      />
    </Paper>
  );
};

export default RouteItinerary;