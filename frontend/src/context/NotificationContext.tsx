import React, { createContext, useContext, useState } from 'react';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';

interface NotificationContextType {
    showSuccess: (message: string) => void;
    showError: (error: any) => void;
    showConfirm: (options: { title?: string; message: string }) => Promise<boolean>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    // Estado para las alertas/snackbars existentes
    const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
        open: false,
        message: '',
        severity: 'success',
    });

    // Estado para el Modal de Confirmación Global
    const [confirmDialog, setConfirmDialog] = useState<{
        open: boolean;
        title: string;
        message: string;
        resolve: (value: boolean) => void;
    } | null>(null);

    // Funciones de notificación comunes
    const showSuccess = (message: string) => {
        setSnackbar({ open: true, message, severity: 'success' });
    };

    const showError = (error: any) => {
        const msg = error?.message || error || 'Ha ocurrido un error inesperado.';
        setSnackbar({ open: true, message: msg, severity: 'error' });
    };

    // Función mágica de Confirmación con Promesas
    const showConfirm = (options: { title?: string; message: string }): Promise<boolean> => {
        return new Promise((resolve) => {
            setConfirmDialog({
                open: true,
                title: options.title || '¿Está seguro?',
                message: options.message,
                resolve,
            });
        });
    };

    const handleCloseSnackbar = () => setSnackbar((prev) => ({ ...prev, open: false }));

    const handleConfirmChoice = (choice: boolean) => {
        if (confirmDialog) {
            confirmDialog.resolve(choice); // Resuelve la promesa con true o false
            setConfirmDialog(null); // Cierra el modal
        }
    };

    return (
        <NotificationContext.Provider value={{ showSuccess, showError, showConfirm }}>
            {children}

            {/* Alertas Globales existentes */}
            <Snackbar open={snackbar.open} autoHideDuration={5000} onClose={handleCloseSnackbar}>
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }} variant="filled">
                    {snackbar.message}
                </Alert>
            </Snackbar>

            {/* MODAL DE CONFIRMACIÓN GLOBAL */}
            <Dialog
                open={!!confirmDialog?.open}
                onClose={() => handleConfirmChoice(false)}
                maxWidth="xs"
                fullWidth
                // Reemplaza PaperProps por slotProps.paper 👇
                slotProps={{
                    paper: {
                        sx: { borderRadius: 2, p: 1 }
                    }
                }}
            >
                <DialogTitle sx={{ fontWeight: 'bold', pb: 1 }}>
                    {confirmDialog?.title}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText color="text.primary">
                        {confirmDialog?.message}
                    </DialogContentText>
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 1 }}>
                    <Button
                        onClick={() => handleConfirmChoice(false)}
                        color="inherit"
                        variant="outlined"
                    >
                        Cancelar
                    </Button>
                    <Button
                        onClick={() => handleConfirmChoice(true)}
                        color="error"
                        variant="contained"
                        autoFocus
                        sx={{ fontWeight: 'bold' }}
                    >
                        Confirmar
                    </Button>
                </DialogActions>
            </Dialog>
        </NotificationContext.Provider>
    );
};

export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotification debe ser usado dentro de un NotificationProvider');
    }
    return context;
};