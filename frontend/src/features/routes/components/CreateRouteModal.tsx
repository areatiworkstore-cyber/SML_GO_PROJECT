import React, { useState, useEffect, useMemo } from 'react';
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
import Checkbox from '@mui/material/Checkbox';
import Chip from '@mui/material/Chip';
import InputAdornment from '@mui/material/InputAdornment';
import Paper from '@mui/material/Paper';

import SearchIcon from '@mui/icons-material/Search';
import PersonPinIcon from '@mui/icons-material/PersonPin';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';

// Importaciones de MUI X Pickers y Dayjs
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs, { Dayjs } from 'dayjs';

import { routeService } from '../services';
import { clientService } from '../../clients/services/services';
import type { Client } from '../../clients/types';
import type { WaypointCreate } from '../types';
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

    // Estados para la gestión de clientes del vendedor
    const [clients, setClients] = useState<Client[]>([]);
    const [loadingClients, setLoadingClients] = useState<boolean>(false);
    const [selectedClientIds, setSelectedClientIds] = useState<number[]>([]);
    const [searchQuery, setSearchQuery] = useState<string>('');

    // Sincronizar y cargar clientes al abrir el modal
    useEffect(() => {
        if (open) {
            setName('');
            setSelectedDate(initialDate ? dayjs(initialDate) : dayjs());
            setSelectedClientIds([]);
            setSearchQuery('');

            if (user?.id) {
                fetchVendorClients(Number(user.id));
            }
        }
    }, [open, initialDate, user?.id]);

    const fetchVendorClients = async (userId: number) => {
        try {
            setLoadingClients(true);
            // Solicitar clientes asignados al vendedor activo
            const data = await clientService.getClients(userId);
            // Filtrar estrictamente solo los clientes pertenecientes a este vendedor
            const vendorOnlyClients = (data || []).filter((c) => Number(c.user_id) === Number(userId));
            setClients(vendorOnlyClients);
        } catch (err: any) {
            showError('Error al cargar la cartera de clientes del vendedor.');
        } finally {
            setLoadingClients(false);
        }
    };

    // Filtrar clientes en tiempo real por búsqueda
    const filteredClients = useMemo(() => {
        if (!searchQuery.trim()) return clients;
        const q = searchQuery.toLowerCase().trim();
        return clients.filter(
            (c) =>
                (c.name && c.name.toLowerCase().includes(q)) ||
                (c.code && c.code.toLowerCase().includes(q)) ||
                (c.document_number && c.document_number.toLowerCase().includes(q)) ||
                (c.address && c.address.toLowerCase().includes(q))
        );
    }, [clients, searchQuery]);

    // Manejadores de selección de clientes
    const toggleClient = (id: number) => {
        setSelectedClientIds((prev) =>
            prev.includes(id) ? prev.filter((clientId) => clientId !== id) : [...prev, id]
        );
    };

    const handleSelectAllFiltered = () => {
        const filteredIds = filteredClients.map((c) => c.id);
        const newSelected = Array.from(new Set([...selectedClientIds, ...filteredIds]));
        setSelectedClientIds(newSelected);
    };

    const handleDeselectAll = () => {
        setSelectedClientIds([]);
    };

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

            // Construir los waypoints ordenados a partir de los clientes seleccionados
            const waypointsToCreate: WaypointCreate[] = selectedClientIds.map((clientId, idx) => {
                const client = clients.find((c) => c.id === clientId);
                return {
                    client_id: clientId,
                    address: client?.address || 'Sin dirección registrada',
                    latitud: client?.latitud ?? undefined,
                    longitud: client?.longitud ?? undefined,
                    order_sequence: idx + 1,
                    status: 'PENDIENTE',
                };
            });

            // 1. Crear la ruta con los waypoints asignados
            const createdRoute = await routeService.createRoute({
                name: name.trim(),
                scheduled_date: selectedDate.format('YYYY-MM-DD'),
                user_id: Number(user.id),
                active: true,
                waypoints: waypointsToCreate,
            });

            // 2. Fallback: Si por compatibilidad con el backend la ruta no persistió los waypoints en la llamada inicial
            if (
                createdRoute &&
                createdRoute.id &&
                waypointsToCreate.length > 0 &&
                (!createdRoute.waypoints || createdRoute.waypoints.length === 0)
            ) {
                for (const wp of waypointsToCreate) {
                    await routeService.createWaypoint(createdRoute.id, wp);
                }
            }

            const totalAssigned = selectedClientIds.length;
            showSuccess(
                totalAssigned > 0
                    ? `¡Ruta creada exitosamente con ${totalAssigned} cliente(s) asignado(s)!`
                    : '¡Ruta de venta creada exitosamente!'
            );

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
            maxWidth="sm"
            fullWidth
            slotProps={{
                paper: {
                    sx: { borderRadius: 3, p: 1 }
                }
            }}
        >
            <DialogTitle sx={{ fontWeight: 'bold', pb: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <span>📍</span>
                    <Typography variant="h6" component="span" sx={{ fontWeight: 'bold' }}>
                        Crear Nueva Ruta de Venta
                    </Typography>
                </Box>
                {selectedClientIds.length > 0 && (
                    <Chip
                        label={`${selectedClientIds.length} seleccionado(s)`}
                        color="primary"
                        size="small"
                        sx={{ fontWeight: 'bold' }}
                    />
                )}
            </DialogTitle>

            <form onSubmit={handleSubmit}>
                <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                        {/* SECCIÓN 1: DATOS GENERALES DE LA RUTA */}
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                            <Typography variant="subtitle2" color="primary" sx={{ fontWeight: 'bold', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                                📌 Datos de la Ruta
                            </Typography>
                            <Grid container spacing={2}>
                                <Grid size={{ xs: 12, sm: 7 }}>
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
                                </Grid>
                                <Grid size={{ xs: 12, sm: 5 }}>
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
                                </Grid>
                            </Grid>
                        </Box>

                        {/* SECCIÓN 2: ASIGNACIÓN DE CLIENTES DEL VENDEDOR */}
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <PersonPinIcon color="primary" fontSize="small" />
                                    <Typography variant="subtitle2" color="primary" sx={{ fontWeight: 'bold', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                                        Clientes de tu Cartera
                                    </Typography>
                                </Box>
                                <Chip
                                    label={`${clients.length} cliente(s) disponible(s)`}
                                    variant="outlined"
                                    size="small"
                                    sx={{ fontWeight: 'medium', fontSize: '0.75rem' }}
                                />
                            </Box>

                            {/* Buscador de clientes */}
                            <TextField
                                fullWidth
                                size="small"
                                placeholder="Buscar por código, nombre, RUC/DNI o dirección..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                disabled={submitting || loadingClients || clients.length === 0}
                                slotProps={{
                                    input: {
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <SearchIcon fontSize="small" color="action" />
                                            </InputAdornment>
                                        ),
                                    }
                                }}
                            />

                            {/* Botones de selección masiva */}
                            {filteredClients.length > 0 && (
                                <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                                    <Button
                                        size="small"
                                        onClick={handleSelectAllFiltered}
                                        disabled={submitting}
                                        sx={{ fontSize: '0.75rem', py: 0.2 }}
                                    >
                                        Seleccionar Filtrados ({filteredClients.length})
                                    </Button>
                                    {selectedClientIds.length > 0 && (
                                        <Button
                                            size="small"
                                            color="secondary"
                                            onClick={handleDeselectAll}
                                            disabled={submitting}
                                            sx={{ fontSize: '0.75rem', py: 0.2 }}
                                        >
                                            Desmarcar Todos
                                        </Button>
                                    )}
                                </Box>
                            )}

                            {/* Lista de clientes del vendedor */}
                            <Paper
                                variant="outlined"
                                sx={{
                                    maxHeight: 260,
                                    overflowY: 'auto',
                                    borderRadius: 2,
                                    borderColor: 'divider',
                                    bgcolor: 'background.default',
                                    p: 1,
                                }}
                            >
                                {loadingClients ? (
                                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', p: 4, gap: 1.5 }}>
                                        <CircularProgress size={24} />
                                        <Typography variant="body2" color="text.secondary">
                                            Cargando clientes de tu cartera...
                                        </Typography>
                                    </Box>
                                ) : clients.length === 0 ? (
                                    <Box sx={{ p: 3, textCenter: 'center', textAlign: 'center' }}>
                                        <Typography variant="body2" color="text.secondary">
                                            No tienes clientes asignados a tu cartera comercial.
                                        </Typography>
                                    </Box>
                                ) : filteredClients.length === 0 ? (
                                    <Box sx={{ p: 3, textAlign: 'center' }}>
                                        <Typography variant="body2" color="text.secondary">
                                            No se encontraron clientes que coincidan con "{searchQuery}".
                                        </Typography>
                                    </Box>
                                ) : (
                                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                        {filteredClients.map((client) => {
                                            const isSelected = selectedClientIds.includes(client.id);
                                            return (
                                                <Paper
                                                    key={client.id}
                                                    elevation={0}
                                                    onClick={() => !submitting && toggleClient(client.id)}
                                                    sx={{
                                                        p: 1.2,
                                                        px: 1.5,
                                                        borderRadius: 1.5,
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        gap: 1.5,
                                                        cursor: submitting ? 'default' : 'pointer',
                                                        border: '1px solid',
                                                        borderColor: isSelected ? 'primary.main' : 'transparent',
                                                        bgcolor: isSelected ? 'action.selected' : 'background.paper',
                                                        transition: 'all 0.15s ease',
                                                        '&:hover': {
                                                            bgcolor: isSelected ? 'action.selected' : 'action.hover',
                                                        },
                                                    }}
                                                >
                                                    <Checkbox
                                                        size="small"
                                                        checked={isSelected}
                                                        disabled={submitting}
                                                        icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
                                                        checkedIcon={<CheckBoxIcon fontSize="small" />}
                                                        sx={{ p: 0 }}
                                                    />
                                                    <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.2 }}>
                                                            <Chip
                                                                label={client.code || `#${client.id}`}
                                                                size="small"
                                                                color={isSelected ? 'primary' : 'default'}
                                                                variant={isSelected ? 'filled' : 'outlined'}
                                                                sx={{ height: 20, fontSize: '0.7rem', fontWeight: 'bold' }}
                                                            />
                                                            <Typography
                                                                variant="subtitle2"
                                                                noWrap
                                                                sx={{
                                                                    fontWeight: isSelected ? 'bold' : 'medium',
                                                                    color: isSelected ? 'primary.main' : 'text.primary',
                                                                }}
                                                            >
                                                                {client.name}
                                                            </Typography>
                                                        </Box>
                                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                                            <LocationOnIcon sx={{ fontSize: '0.85rem', color: 'text.secondary' }} />
                                                            <Typography
                                                                variant="caption"
                                                                color="text.secondary"
                                                                noWrap
                                                                sx={{ fontSize: '0.75rem' }}
                                                            >
                                                                {client.address || 'Sin dirección registrada'}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </Paper>
                                            );
                                        })}
                                    </Box>
                                )}
                            </Paper>
                        </Box>
                    </LocalizationProvider>
                </DialogContent>

                <DialogActions sx={{ p: 2, px: 3, gap: 1, justifyContent: 'space-between' }}>
                    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 'medium' }}>
                        {selectedClientIds.length > 0
                            ? `✓ ${selectedClientIds.length} cliente(s) seleccionado(s) para esta ruta`
                            : 'Ningún cliente seleccionado'}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1 }}>
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
                    </Box>
                </DialogActions>
            </form>
        </Dialog>
    );
};
