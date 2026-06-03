import React, { useState } from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';

import type { SupplierResponse } from '../types';
import { supplierService } from '../services';
import { useNotification } from '../../../context/NotificationContext';

interface QuickSupplierModalProps {
  open: boolean;
  supplierCode: string;
  onClose: () => void;
  onSuccess: (createdSupplier: SupplierResponse) => void;
}

export const QuickSupplierModal: React.FC<QuickSupplierModalProps> = ({
  open,
  supplierCode,
  onClose,
  onSuccess,
}) => {
  const { showSuccess, showError } = useNotification();
  const [names, setNames] = useState<string>('');
  const [creating, setCreating] = useState<boolean>(false);

  if (!open) return null;

  const handleCreate = async () => {
    if (!names.trim()) {
      showError('Ingrese el nombre o razón social del proveedor.');
      return;
    }

    setCreating(true);
    try {
      const created = await supplierService.createSupplier({
        code: supplierCode.trim().toUpperCase(),
        names: names.trim(),
        active: true,
      });

      showSuccess(`¡Proveedor ${created.code} creado con éxito!`);
      setNames('');
      onSuccess(created);
    } catch (err: any) {
      showError('Ocurrió un error al intentar registrar el proveedor.');
    } finally {
      setCreating(false);
    }
  };

  return (
    <Box
      sx={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100vw',
        height: '100vh',
        bgcolor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 9999,
      }}
    >
      <Paper
        sx={{
          p: 4,
          width: '90%',
          maxWidth: 450,
          borderRadius: 3,
          boxShadow: 24,
        }}
      >
        <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1, color: 'primary.main' }}>
          Registrar Nuevo Proveedor
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          El código <strong>{supplierCode.toUpperCase()}</strong> no existe. ¿Deseas darlo de alta en este momento?
        </Typography>

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          <TextField
            disabled
            fullWidth
            label="Código del Proveedor"
            value={supplierCode.toUpperCase()}
            variant="outlined"
          />
          <TextField
            autoFocus
            required
            fullWidth
            label="Nombre / Razón Social"
            placeholder="Ej. Importaciones SAC"
            value={names}
            onChange={(e) => setNames(e.target.value)}
            variant="outlined"
          />

          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
            <Button
              variant="outlined"
              color="inherit"
              fullWidth
              onClick={() => {
                setNames('');
                onClose();
              }}
              disabled={creating}
            >
              Omitir
            </Button>
            <Button
              variant="contained"
              color="primary"
              fullWidth
              onClick={handleCreate}
              disabled={creating}
              sx={{ color: 'white', fontWeight: 'bold' }}
            >
              {creating ? <CircularProgress size={24} color="inherit" /> : 'Crear y Vincular'}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};