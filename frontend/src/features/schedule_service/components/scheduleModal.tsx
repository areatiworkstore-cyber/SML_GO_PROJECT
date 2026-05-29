import React, { useState, useEffect } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import CircularProgress from '@mui/material/CircularProgress';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';

// Importaciones de MUI X Pickers y Dayjs
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { StaticTimePicker } from '@mui/x-date-pickers/StaticTimePicker';
import dayjs, { Dayjs } from 'dayjs';

import { scheduleService } from '../services';
import { clientService } from '../../clients/services';
import { useNotification } from '../../../context/NotificationContext';
import { useAuth } from '../../auth/context/AuthContext';
import type { Client } from '../../clients/types';

interface ScheduleModalProps {
    open: boolean;
    onClose: () => void;
    initialDate: string; // Recibe la fecha del día seleccionado en el itinerario (YYYY-MM-DD)
}

export const ScheduleModal: React.FC<ScheduleModalProps> = ({ open, onClose, initialDate }) => {
    const { showSuccess, showError } = useNotification();
    const { user } = useAuth();

    // Estados del formulario y búsqueda
    const [allClients, setAllClients] = useState<Client[]>([]);
    const [searchQuery, setSearchQuery] = useState<string>(''); // Texto ingresado (Código o Documento)
    const [selectedClient, setSelectedClient] = useState<Client | null>(null); // Objeto cliente encontrado

    const [selectedDate, setSelectedDate] = useState<Dayjs | null>(null);
    const [selectedTime, setSelectedTime] = useState<Dayjs | null>(dayjs().hour(8).minute(0));

    const [loadingClients, setLoadingClients] = useState<boolean>(false);
    const [submitting, setSubmitting] = useState<boolean>(false);

    // Sincronizar la fecha de la pestaña activa cuando se abre el modal
    useEffect(() => {
        if (open && initialDate) {
            setSelectedDate(dayjs(initialDate));
            setSelectedClient(null);
            setSearchQuery('');
            setSelectedTime(dayjs(`${initialDate}T08:00:00`));

            // Cargar el universo de clientes asignados al abrir el modal
            const fetchClients = async () => {
                try {
                    setLoadingClients(true);
                    const data = await clientService.getClients?.() || [];
                    setAllClients(data);
                } catch (err) {
                    console.error("Error cargando clientes:", err);
                } finally {
                    setLoadingClients(false);
                }
            };

            fetchClients();
        }
    }, [open, initialDate]);

    // Lógica del motor de búsqueda mediante el botón de lupa o Enter
    const handleSearchClient = () => {
        if (!searchQuery.trim()) {
            showError('Ingrese un código o número de documento para buscar.');
            return;
        }

        const cleanQuery = searchQuery.trim().toLowerCase();

        // Buscar coincidencia exacta o parcial por código o número de documento
        const found = allClients.find(
            (c) => c.code.toLowerCase() === cleanQuery || c.document_number === cleanQuery
        );

        if (found) {
            setSelectedClient(found);
        } else {
            setSelectedClient(null);
            showError('Cliente no encontrado. Verifique el código o documento.');
        }
    };

    const handleSubmit = async () => {
        if (!user?.id) {
            showError('No se pudo determinar el usuario activo. Inicie sesión nuevamente.');
            return;
        }

        if (!selectedClient || !selectedDate || !selectedTime) {
            showError('Por favor, busque y seleccione un cliente válido antes de guardar.');
            return;
        }

        try {
            setSubmitting(true);

            const formattedDate = selectedDate.format('YYYY-MM-DD');
            const formattedTime = selectedTime.format('HH:mm:ss');

            await scheduleService.createSchedule({
                client_id: Number(selectedClient.id),
                user_id: Number(user.id),
                day: formattedDate,
                start_time: formattedTime,
                active: true
            });

            showSuccess('¡Visita programada exitosamente!');
            onClose();
        } catch (err: any) {
            showError(err);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="md"
            fullWidth
            slotProps={{
                paper: {
                    sx: { borderRadius: 3, p: 1 }
                }
            }}
        >
            <DialogTitle sx={{ fontWeight: 'bold', pb: 1 }}>
                📅 Programar Nueva Visita Comercial
            </DialogTitle>

            <DialogContent dividers>
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                    <Grid container spacing={3} sx={{ mt: 0.5 }}>

                        {/* COLUMNA IZQUIERDA: BÚSQUEDA DE CLIENTE Y FECHA */}
                        <Grid size={{ xs: 12, md: 6 }}>
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>

                                {/* CAMPO DE BÚSQUEDA CON LUPA */}
                                <Box>
                                    <TextField
                                        fullWidth
                                        label="Buscar Cliente (Código o N° Documento)"
                                        variant="outlined"
                                        required
                                        value={searchQuery}
                                        onChange={(e) => setSearchQuery(e.target.value)}
                                        onKeyDown={(e) => {
                                            if (e.key === 'Enter') {
                                                e.preventDefault();
                                                handleSearchClient();
                                            }
                                        }}
                                        disabled={loadingClients || submitting}
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <IconButton
                                                            onClick={handleSearchClient}
                                                            edge="end"
                                                            disabled={loadingClients || submitting}
                                                        >
                                                            {loadingClients ? <CircularProgress size={20} /> : '🔍'}
                                                        </IconButton>
                                                    </InputAdornment>
                                                ),
                                            }
                                        }}
                                    />

                                    {/* CONTENEDOR CON LOS DATOS EXCLUSIVOS DEL CLIENTE ENCONTRADO */}
                                    {selectedClient && (
                                        <Box
                                            sx={{
                                                mt: 1.5,
                                                p: 2,
                                                borderRadius: 2,
                                                bgcolor: 'rgba(242, 146, 0, 0.05)',
                                                border: '1px solid',
                                                borderColor: 'primary.main'
                                            }}
                                        >
                                            <Typography variant="subtitle2" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                                                Cliente Seleccionado:
                                            </Typography>
                                            <Typography variant="body1" sx={{ fontWeight: 'bold', mt: 0.5 }}>
                                                {selectedClient.name}
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary">
                                                Código: <strong>{selectedClient.code}</strong> | Doc: {selectedClient.document_number}
                                            </Typography>
                                        </Box>
                                    )}
                                </Box>

                                {/* PICKER DE FECHA CONTROLADO */}
                                <DatePicker
                                    label="Fecha de la Visita"
                                    value={selectedDate}
                                    onChange={(newValue) => setSelectedDate(newValue)}
                                    slotProps={{
                                        textField: {
                                            fullWidth: true,
                                            required: true,
                                        },
                                    }}
                                    disabled={submitting}
                                />

                                <Box sx={{ p: 2, bgcolor: 'action.hover', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
                                    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 'bold', display: 'block', mb: 0.5 }}>
                                        NOTA DE ASIGNACIÓN
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        La parada se indexará automáticamente en el timeline del día seleccionado respetando el orden cronológico de la hora asignada.
                                    </Typography>
                                </Box>
                            </Box>
                        </Grid>

                        {/* COLUMNA DERECHA: RELOJ ESTÁTICO CORREGIDO */}
                        <Grid size={{ xs: 12, md: 6 }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                <Typography variant="subtitle2" sx={{ fontWeight: 'bold', color: 'text.secondary' }}>
                                    Seleccionar Hora de la Parada
                                </Typography>

                                {/* Mostrar la hora formateada dinámicamente con color de texto adaptativo */}
                                {selectedTime && (
                                    <Typography
                                        variant="subtitle2"
                                        sx={{
                                            fontWeight: 'bold',
                                            bgcolor: 'primary.main',
                                            // ✅ contrastText cambia automáticamente entre blanco/negro según el fondo del tema activo
                                            color: 'primary.contrastText',
                                            px: 1.5,
                                            py: 0.2,
                                            borderRadius: 1
                                        }}
                                    >
                                        {selectedTime.format('hh:mm A')}
                                    </Typography>
                                )}
                            </Box>

                            <Box
                                sx={{
                                    border: '1px solid',
                                    borderColor: 'divider',
                                    borderRadius: 2,
                                    overflow: 'hidden',
                                    backgroundColor: 'background.paper'
                                }}
                            >
                                <StaticTimePicker
                                    displayStaticWrapperAs="desktop"
                                    value={selectedTime}
                                    onChange={(newValue) => setSelectedTime(newValue)}
                                    disabled={submitting}
                                    slotProps={{
                                        actionBar: {
                                            actions: []
                                        }
                                    }}
                                />
                            </Box>
                        </Grid>

                    </Grid>
                </LocalizationProvider>
            </DialogContent>

            <DialogActions sx={{ p: 2, gap: 1 }}>
                <Button
                    onClick={onClose}
                    color="inherit"
                    disabled={submitting}
                    sx={{ fontWeight: 'bold' }}
                >
                    Cancelar
                </Button>
                <Button
                    onClick={handleSubmit}
                    variant="contained"
                    color="primary"
                    disabled={submitting || !selectedClient}
                    sx={{ fontWeight: 'bold', color: 'secondary.main', px: 3 }}
                >
                    {submitting ? <CircularProgress size={24} color="inherit" /> : 'Guardar Parada'}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ScheduleModal;