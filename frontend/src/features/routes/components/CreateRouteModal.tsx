import React, { useState, useEffect } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import CircularProgress from '@mui/material/CircularProgress';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';

// Importaciones de MUI X Pickers y Dayjs
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs, { Dayjs } from 'dayjs';

import { routeService } from '../services';
import { useNotification } from '../../../context/NotificationContext';
import { useAuth } from '../../auth/context/AuthContext';

interface CreateRouteModalProps {
    open: boolean;
    onClose: () => void;
    onSuccess?: () => void;
    initialDate?: string;
}

export const CreateRouteModal: React.FC<CreateRouteModalProps> = ({ open, onClose, onSuccess, initialDate }) => {
    const { showSuccess, showError } = useNotification();
    const { user } = useAuth();

    // Estados del formulario
    const [name, setName] = useState<string>('');
    const [selectedDate, setSelectedDate] = useState<Dayjs | null>(null);
    const [submitting, setSubmitting] = useState<boolean>(false);

    // Sincronizar fecha inicial al abrir el modal
    useEffect(() => {
        if (open) {
            setName('');
            setSelectedDate(initialDate ? dayjs(initialDate) : dayjs());
        }
    }, [open, initialDate]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!user?.id) {
            showError('No se pudo determinar el usuario activo. Inicie sesión nuevamente.');
            return;
        }

        if (!name.trim()) {
            showError('Por favor, ingrese un nombre para la ruta.');
            return;
        }

        if (!selectedDate) {
            showError('Por favor, seleccione una fecha programada.');
            return;
        }

        try {
            setSubmitting(true);

            await routeService.createRoute({
                name: name.trim(),
                scheduled_date: selectedDate.format('YYYY-MM-DD'),
                user_id: Number(user.id),
                active: true,
                waypoints: [] // Se crea inicialmente vacía según la especificación
            });

            showSuccess('¡Ruta comercial creada exitosamente!');
            if (onSuccess) onSuccess();
            onClose();
        } catch (err: any) {
            showError(err.message || 'Error al intentar crear la ruta.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="xs"
            fullWidth
            slotProps={{
                paper: {
                    sx: { borderRadius: 3, p: 1 }
                }
            }}
        >
            <DialogTitle sx={{ fontWeight: 'bold', pb: 1 }}>
                📍 Crear Nueva Ruta de Venta
            </DialogTitle>

            <form onSubmit={handleSubmit}>
                <DialogContent dividers>
                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                        <Grid container spacing={3.5} sx={{ mt: 0.5 }}>
                            <Grid item xs={12}>
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
                                    <TextField
                                        fullWidth
                                        label="Nombre de la Ruta"
                                        variant="outlined"
                                        required
                                        placeholder="Ej: Ruta Norte - Distribuidores"
                                        value={name}
                                        onChange={(e) => setName(e.target.value)}
                                        disabled={submitting}
                                        slotProps={{
                                            inputLabel: { shrink: true }
                                        }}
                                    />

                                    <DatePicker
                                        label="Fecha Programada"
                                        value={selectedDate}
                                        onChange={(newValue) => setSelectedDate(newValue)}
                                        slotProps={{
                                            textField: {
                                                fullWidth: true,
                                                required: true,
                                                slotProps: {
                                                    inputLabel: { shrink: true }
                                                }
                                            },
                                        }}
                                        disabled={submitting}
                                    />

                                    <Box sx={{ p: 2, bgcolor: 'action.hover', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
                                        <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 'bold', display: 'block', mb: 0.5 }}>
                                            INFORMACIÓN
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                            Esta ruta servirá como base para asociar paradas, clientes y secuencias de visitas comerciales en la fecha establecida.
                                        </Typography>
                                    </Box>
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
                        type="submit"
                        variant="contained"
                        color="primary"
                        disabled={submitting || !name.trim()}
                        sx={{ fontWeight: 'bold', color: 'secondary.main', px: 3 }}
                    >
                        {submitting ? <CircularProgress size={24} color="inherit" /> : 'Crear Ruta'}
                    </Button>
                </DialogActions>
            </form>
        </Dialog>
    );
};
