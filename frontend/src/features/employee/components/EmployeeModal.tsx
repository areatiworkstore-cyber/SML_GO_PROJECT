import React, { useEffect } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { useForm, Controller } from 'react-hook-form';
import type { Employee } from '../types';

interface EmployeeModalProps {
    open: boolean;
    onClose: () => void;
    onSave: (data: any) => void;
    employee: Employee | null;
    mode: 'create' | 'edit' | 'view';
}

export const EmployeeModal: React.FC<EmployeeModalProps> = ({ open, onClose, onSave, employee, mode }) => {
    const { control, handleSubmit, reset } = useForm({
        defaultValues: {
            code: '',
            fullName: '',
            document_number: '',
            role: 'VENDEDOR',
            email: '',
            phone: '',
            status: 'ACTIVO'
        }
    });

    useEffect(() => {
        if (employee) {
            reset(employee);
        } else {
            reset({
                code: '',
                fullName: '',
                document_number: '',
                role: 'VENDEDOR',
                email: '',
                phone: '',
                status: 'ACTIVO'
            });
        }
    }, [employee, open, reset]);

    const isView = mode === 'view';

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth slotProps={{ backdrop: { style: { backdropFilter: 'blur(4px)' } } }}>
            <DialogTitle sx={{ fontWeight: 'bold', borderBottom: '1px solid', borderColor: 'divider' }}>
                {mode === 'create' && '✨ Registrar Nuevo Empleado'}
                {mode === 'edit' && '✏️ Editar Empleado'}
                {mode === 'view' && '📋 Información Completa del Empleado'}
            </DialogTitle>

            <form onSubmit={handleSubmit(onSave)}>
                <DialogContent sx={{ p: 3 }}>
                    <Grid container spacing={2}>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="code"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} label="Código de Empleado" fullWidth disabled={isView || mode === 'edit'} size="small" required />
                                )}
                            />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="document_number"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} label="DNI / Documento" fullWidth disabled={isView} size="small" required />
                                )}
                            />
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                            <Controller
                                name="fullName"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} label="Nombre Completo / Razón Social" fullWidth disabled={isView} size="small" required />
                                )}
                            />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="role"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} select label="Rol de Sistema" fullWidth disabled={isView} size="small">
                                        <MenuItem value="VENDEDOR">Asesor de Ventas (Vendedor)</MenuItem>
                                        <MenuItem value="ADMIN">Administrador</MenuItem>
                                        <MenuItem value="SUPERVISOR">Supervisor de Campo</MenuItem>
                                    </TextField>
                                )}
                            />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="status"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} select label="Estado" fullWidth disabled={isView} size="small">
                                        <MenuItem value="ACTIVO">Activo</MenuItem>
                                        <MenuItem value="INACTIVO">Inactivo</MenuItem>
                                    </TextField>
                                )}
                            />
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                            <Controller
                                name="email"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} type="email" label="Correo Electrónico" fullWidth disabled={isView} size="small" required />
                                )}
                            />
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                            <Controller
                                name="phone"
                                control={control}
                                render={({ field }) => (
                                    <TextField {...field} label="Teléfono / Celular" fullWidth disabled={isView} size="small" />
                                )}
                            />
                        </Grid>
                    </Grid>
                </DialogContent>

                <DialogActions sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
                    <Button onClick={onClose} variant="outlined" color="inherit">
                        {isView ? 'Cerrar' : 'Cancelar'}
                    </Button>
                    {!isView && (
                        <Button type="submit" variant="contained" color="primary" sx={{ fontWeight: 'bold' }}>
                            Guardar Cambios
                        </Button>
                    )}
                </DialogActions>
            </form>
        </Dialog>
    );
};