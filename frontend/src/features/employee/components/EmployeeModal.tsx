import React, { useEffect, useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Grid from '@mui/material/Grid';
import CircularProgress from '@mui/material/CircularProgress';
import InputAdornment from '@mui/material/InputAdornment';
import { useForm, Controller } from 'react-hook-form';
import type { Employee } from '../types';
import type { DocumentTypeResponse } from '../../../types';
// Supongo que el tipo de respuesta de roles está en la misma ruta o se infiere de la estructura habitual
import { masterDataService } from '../../../services/masterDataService';

interface EmployeeModalProps {
    open: boolean;
    onClose: () => void;
    onSave: (data: any) => void;
    employee: Employee | null;
    mode: 'create' | 'edit' | 'view';
}

export const EmployeeModal: React.FC<EmployeeModalProps> = ({ open, onClose, onSave, employee, mode }) => {
    const [documentTypes, setDocumentTypes] = useState<DocumentTypeResponse[]>([]);
    const [isLoadingDocs, setIsLoadingDocs] = useState<boolean>(false);

    // Estados añadidos para el manejo dinámico de roles
    const [roles, setRoles] = useState<any[]>([]);
    const [isLoadingRoles, setIsLoadingRoles] = useState<boolean>(false);

    const { control, handleSubmit, reset } = useForm({
        defaultValues: {
            code: '',
            first_name: '',
            second_name: '',
            first_surname: '',
            second_surname: '',
            document_type_id: '',
            document_number: '',
            cellphone: '',
            email: '',
            password: '',
            role: '',
        }
    });

    useEffect(() => {
        const fetchMasterData = async () => {
            setIsLoadingDocs(true);
            setIsLoadingRoles(true);

            // Petición de Tipos de Documento
            try {
                const res = await masterDataService.getDocumentTypes();
                setDocumentTypes(res || []);
            } catch (error) {
                console.error("Error cargando tipos de documento:", error);
                setDocumentTypes([]);
            } finally {
                setIsLoadingDocs(false);
            }

            // Petición de Roles del Sistema
            try {
                const res = await masterDataService.getRoles();
                setRoles(res || []);
            } catch (error) {
                console.error("Error cargando roles del sistema:", error);
                setRoles([]);
            } finally {
                setIsLoadingRoles(false);
            }
        };

        if (open) {
            fetchMasterData();
        }
    }, [open]);

    useEffect(() => {
        if (employee) {
            reset({
                ...employee,
                password: ''
            });
        } else {
            reset({
                code: '',
                first_name: '',
                second_name: '',
                first_surname: '',
                second_surname: '',
                document_type_id: '',
                document_number: '',
                cellphone: '',
                email: '',
                password: '',
                role: ''
            });
        }
    }, [employee, open, reset]);

    const isView = mode === 'view';
    const isCreate = mode === 'create';

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth slotProps={{ backdrop: { style: { backdropFilter: 'blur(4px)' } } }}>
            <DialogTitle sx={{ fontWeight: 'bold', borderBottom: '1px solid', borderColor: 'divider' }}>
                {mode === 'create' && '✨ Registrar Nuevo Empleado'}
                {mode === 'edit' && '✏️ Editar Empleado'}
                {mode === 'view' && '📋 Información Completa del Empleado'}
            </DialogTitle>

            <form onSubmit={handleSubmit(onSave)}>
                <DialogContent sx={{ p: 3 }}>
                    <Grid container spacing={2.5} sx={{ mt: 0.5 }}>

                        {/* CÓDIGO DE EMPLEADO */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="code"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Código de Empleado"
                                        fullWidth
                                        disabled={isView || mode === 'edit'}
                                        size="small"
                                        required
                                        slotProps={{ htmlInput: { maxLength: 6 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* CORREO ELECTRÓNICO */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="email"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        type="email"
                                        label="Correo Electrónico"
                                        fullWidth
                                        disabled={isView}
                                        size="small"
                                        required
                                        slotProps={{ htmlInput: { maxLength: 30 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* PRIMER NOMBRE */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="first_name"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Primer Nombre"
                                        fullWidth
                                        disabled={isView}
                                        size="small"
                                        required
                                        slotProps={{ htmlInput: { maxLength: 50 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* SEGUNDO NOMBRE */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="second_name"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Segundo Nombre"
                                        fullWidth
                                        disabled={isView}
                                        size="small"
                                        slotProps={{ htmlInput: { maxLength: 50 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* PRIMER APELLIDO */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="first_surname"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Apellido Paterno"
                                        fullWidth
                                        disabled={isView}
                                        size="small"
                                        required
                                        slotProps={{ htmlInput: { maxLength: 50 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* SEGUNDO APELLIDO */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="second_surname"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Apellido Materno"
                                        fullWidth
                                        disabled={isView}
                                        size="small"
                                        required
                                        slotProps={{ htmlInput: { maxLength: 50 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* TIPO DE DOCUMENTO */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="document_type_id"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        select
                                        label="Tipo de Documento"
                                        fullWidth
                                        disabled={isView || isLoadingDocs}
                                        size="small"
                                        required
                                        slotProps={{
                                            input: {
                                                endAdornment: isLoadingDocs ? (
                                                    <InputAdornment position="end" sx={{ mr: 2 }}>
                                                        <CircularProgress size={20} color="inherit" />
                                                    </InputAdornment>
                                                ) : null,
                                            }
                                        }}
                                    >
                                        {isLoadingDocs ? (
                                            <MenuItem disabled value="">
                                                Cargando documentos...
                                            </MenuItem>
                                        ) : documentTypes.length === 0 ? (
                                            <MenuItem disabled value="">
                                                No existe ningún registro
                                            </MenuItem>
                                        ) : (
                                            documentTypes.map((type) => (
                                                <MenuItem key={type.id} value={type.id}>
                                                    {type.description}
                                                </MenuItem>
                                            ))
                                        )}
                                    </TextField>
                                )}
                            />
                        </Grid>

                        {/* NÚMERO DE DOCUMENTO */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="document_number"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="N° Documento"
                                        fullWidth
                                        disabled={isView}
                                        size="small"
                                        required
                                        slotProps={{ htmlInput: { maxLength: 11 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* CELULAR */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Controller
                                name="cellphone"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Celular"
                                        fullWidth
                                        disabled={isView}
                                        size="small"
                                        required
                                        slotProps={{ htmlInput: { maxLength: 9 } }}
                                    />
                                )}
                            />
                        </Grid>

                        {/* CONTRASEÑA */}
                        {isCreate && (
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <Controller
                                    name="password"
                                    control={control}
                                    render={({ field }) => (
                                        <TextField
                                            {...field}
                                            type="password"
                                            label="Contraseña"
                                            fullWidth
                                            size="small"
                                            required
                                            slotProps={{ htmlInput: { maxLength: 255 } }}
                                        />
                                    )}
                                />
                            </Grid>
                        )}

                        {/* ROL DE SISTEMA - ACTUALIZADO A DINÁMICO */}
                        <Grid size={{ xs: 12, sm: isCreate ? 6 : 12 }}>
                            <Controller
                                name="role"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        select
                                        label="Rol de Sistema"
                                        fullWidth
                                        disabled={isView || isLoadingRoles}
                                        size="small"
                                        required
                                        slotProps={{
                                            input: {
                                                endAdornment: isLoadingRoles ? (
                                                    <InputAdornment position="end" sx={{ mr: 2 }}>
                                                        <CircularProgress size={20} color="inherit" />
                                                    </InputAdornment>
                                                ) : null,
                                            }
                                        }}
                                    >
                                        {isLoadingRoles ? (
                                            <MenuItem disabled value="">
                                                Cargando roles...
                                            </MenuItem>
                                        ) : roles.length === 0 ? (
                                            <MenuItem disabled value="">
                                                No existen roles registrados
                                            </MenuItem>
                                        ) : (
                                            roles.map((roleItem) => (
                                                <MenuItem key={roleItem.id} value={roleItem.role}>
                                                    {roleItem.role === 'ADMIN' ? 'Administrador' : roleItem.role === 'VENDEDOR' ? 'Asesor de Ventas' : roleItem.role}
                                                </MenuItem>
                                            ))
                                        )}
                                    </TextField>
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

export default EmployeeModal;