import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Chip from '@mui/material/Chip';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import CircularProgress from '@mui/material/CircularProgress';
import type { Employee } from '../types';
import { EmployeeModal } from './EmployeeModal';
import { useNotification } from '../../../context/NotificationContext';
import { employeeService } from '../services';

// Diccionario estático para mapear los IDs de los roles de la base de datos
const ROLE_MAP: Record<number, string> = {
    1: 'ADMIN',
    2: 'VENDEDOR',
    3: 'SUPERVISOR DE CAMPO' // Agregado por si manejas el tercer rol del modal
};

export const EmployeeList: React.FC = () => {
    const { showSuccess, showError, showConfirm } = useNotification();
    const [searchTerm, setSearchTerm] = useState('');
    const [employees, setEmployees] = useState<Employee[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    const [modalState, setModalState] = useState<{
        open: boolean;
        mode: 'create' | 'edit' | 'view';
        data: Employee | null;
    }>({
        open: false,
        mode: 'create',
        data: null
    });

    useEffect(() => {
        loadEmployees();
    }, []);

    const loadEmployees = async () => {
        try {
            setLoading(true);
            const usersData = await employeeService.getEmployees(0, 100);
            setEmployees(usersData || []);
        } catch (error) {
            showError(error);
        } finally {
            setLoading(false);
        }
    };

    const handleOpenModal = (mode: 'create' | 'edit' | 'view', employee: Employee | null = null) => {
        let employeeData = employee;
        if (employee) {
            const userRoleAssignment = employee.roles?.[0];
            const roleId = userRoleAssignment?.role_id;
            employeeData = { ...employee, role: roleId || '' } as any;
        }
        setModalState({ open: true, mode, data: employeeData });
    };

    const handleCloseModal = () => {
        setModalState({ open: false, mode: 'create', data: null });
    };

    const handleSaveEmployee = async (formData: any) => {
        try {
            const payload = {
                ...formData,
                role_ids: formData.role ? [formData.role] : []
            };
            if (modalState.mode === 'create') {
                await employeeService.createEmployee(payload);
                showSuccess('Empleado registrado exitosamente');
            } else if (modalState.mode === 'edit' && modalState.data?.id) {
                await employeeService.updateEmployee(modalState.data.id, payload);
                showSuccess('Empleado actualizado exitosamente');
            }
            handleCloseModal();
            loadEmployees();
        } catch (error) {
            showError(error);
        }
    };

    const handleToggleActive = async (employee: Employee) => {
        const isActive = employee.active !== false;
        const actionText = isActive ? 'desactivar' : 'activar';
        const confirmed = await showConfirm({
            title: isActive ? 'Confirmar Desactivación' : 'Confirmar Activación',
            message: `¿Está seguro de ${actionText} a ${employee.first_name}?`
        });
        if (confirmed) {
            try {
                if (isActive) {
                    await employeeService.deleteEmployee(employee.id);
                    showSuccess('Empleado desactivado correctamente');
                    setEmployees(prev => prev.map(emp => emp.id === employee.id ? { ...emp, active: false } : emp));
                } else {
                    await employeeService.restoreEmployee(employee.id);
                    showSuccess('Empleado activado correctamente');
                    setEmployees(prev => prev.map(emp => emp.id === employee.id ? { ...emp, active: true } : emp));
                }
            } catch (error) {
                showError(error);
            }
        }
    };

    const buildFullName = (emp: Employee) => {
        const parts = [emp.first_name, emp.last_name || (emp as any).first_surname].filter(Boolean);
        return parts.length > 0 ? parts.join(' ') : 'Sin Nombre';
    };

    const filteredEmployees = employees.filter(emp => {
        const fullName = buildFullName(emp).toLowerCase();
        const code = (emp.code || '').toLowerCase();
        const docNum = emp.document_number || '';
        const search = searchTerm.toLowerCase();

        return fullName.includes(search) || code.includes(search) || docNum.includes(search);
    });

    return (
        <Box sx={{ p: { xs: 1, md: 3 } }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
                <Box>
                    <Typography variant="h5" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                        Control de Personal Interno
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        Gestión de accesos, roles y credenciales para el personal de SML Lubricantes.
                    </Typography>
                </Box>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => handleOpenModal('create')}
                    sx={{ fontWeight: 'bold', textTransform: 'none' }}
                >
                    + Nuevo Empleado
                </Button>
            </Box>

            <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider', borderRadius: 4 }}>
                <Box sx={{ mb: 3, display: 'flex', justifyContent: 'flex-end' }}>
                    <TextField
                        placeholder="Buscar por nombre, código o documento..."
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
                    <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
                        <Table sx={{ minWidth: 650 }}>
                            <TableHead sx={{ bgcolor: 'action.hover' }}>
                                <TableRow>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Código</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Nombre Completo</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>N° Documento</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Rol del Sistema</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Correo Electrónico</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Estado</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold', textAlign: 'center' }}>Acciones</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {filteredEmployees.map((emp) => {
                                    const userRoleAssignment = emp.roles?.[0];
                                    const roleId = userRoleAssignment?.role_id;
                                    const systemRole = (roleId && ROLE_MAP[roleId]) ||
                                        userRoleAssignment?.role_details?.role ||
                                        (emp.code?.startsWith('ADM') ? 'ADMIN' : 'VENDEDOR');

                                    return (
                                        <TableRow key={emp.id} sx={{ '&:hover': { bgcolor: 'action.hover' } }}>
                                            <TableCell>
                                                <Chip label={emp.code || `ID-${emp.id}`} size="small" variant="outlined" sx={{ fontWeight: 'bold' }} />
                                            </TableCell>
                                            <TableCell sx={{ fontWeight: 'bold' }}>{buildFullName(emp)}</TableCell>
                                            <TableCell>{emp.document_number}</TableCell>
                                            <TableCell>
                                                <Chip
                                                    label={systemRole.toUpperCase()}
                                                    size="small"
                                                    color={systemRole.toUpperCase() === 'ADMIN' || systemRole.toUpperCase() === 'ADMINISTRADOR' ? 'secondary' : 'info'}
                                                    sx={{ fontWeight: 'bold', borderRadius: 1 }}
                                                />
                                            </TableCell>
                                            <TableCell>{emp.email}</TableCell>
                                            <TableCell>
                                                {emp.active !== false ? (
                                                    <Chip label="Activo" size="small" color="success" sx={{ fontWeight: 'bold' }} />
                                                ) : (
                                                    <Chip label="Inactivo" size="small" color="default" sx={{ fontWeight: 'bold' }} />
                                                )}
                                            </TableCell>
                                            <TableCell align="center">
                                                <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1 }}>
                                                    <Tooltip title="Ver detalles">
                                                        <IconButton size="small" onClick={() => handleOpenModal('view', emp)}>👁️</IconButton>
                                                    </Tooltip>
                                                    <Tooltip title="Editar">
                                                        <IconButton size="small" color="primary" onClick={() => handleOpenModal('edit', emp)}>✏️</IconButton>
                                                    </Tooltip>
                                                    {emp.active !== false ? (
                                                        <Tooltip title="Desactivar">
                                                            <IconButton size="small" color="error" onClick={() => handleToggleActive(emp)}>🚫</IconButton>
                                                        </Tooltip>
                                                    ) : (
                                                        <Tooltip title="Activar">
                                                            <IconButton size="small" color="success" onClick={() => handleToggleActive(emp)}>✅</IconButton>
                                                        </Tooltip>
                                                    )}
                                                </Box>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                                {filteredEmployees.length === 0 && (
                                    <TableRow>
                                        <TableCell colSpan={7} align="center" sx={{ py: 4, color: 'text.secondary', fontStyle: 'italic' }}>
                                            No se encontraron empleados registrados en el sistema.
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )
                }
            </Paper >

            <EmployeeModal
                open={modalState.open}
                mode={modalState.mode}
                employee={modalState.data}
                onClose={handleCloseModal}
                onSave={handleSaveEmployee}
            />
        </Box >
    );
};

export default EmployeeList;