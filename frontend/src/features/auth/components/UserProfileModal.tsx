import React, { useState, useEffect } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import IconButton from '@mui/material/IconButton';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import { useTheme } from '@mui/material/styles';
import CloseIcon from '@mui/icons-material/Close';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { apiClient } from '../../../services/api';

// ─── Tipos ────────────────────────────────────────────────────────────────────

interface RoleDetail {
  id?: number;
  role?: string;
}

interface RoleUserItem {
  id: number;
  user_id: number;
  role_id: number;
  role_details?: RoleDetail | null;
}

interface DocumentType {
  id?: number;
  document_type?: string;
}

interface UserProfile {
  id: number;
  code: string;
  first_name: string;
  second_name: string;
  first_surname: string;
  second_surname: string;
  document_type?: DocumentType | null;
  document_number: string;
  cellphone: string;
  email: string;
  active: boolean;
  roles: RoleUserItem[];
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

function buildFullName(p: UserProfile): string {
  return [p.first_name, p.second_name, p.first_surname, p.second_surname]
    .filter(Boolean)
    .join(' ');
}

function getRoleLabel(p: UserProfile): string {
  if (!p.roles || p.roles.length === 0) return 'Sin rol asignado';
  const names = p.roles
    .map(r => r.role_details?.role)
    .filter(Boolean) as string[];
  return names.length > 0 ? names.join(', ') : 'Sin rol asignado';
}

// ─── Fila de detalle ─────────────────────────────────────────────────────────

interface DetailRowProps {
  icon: string;
  label: string;
  value: React.ReactNode;
}

const DetailRow: React.FC<DetailRowProps> = ({ icon, label, value }) => (
  <Box
    sx={{
      display: 'flex',
      alignItems: 'flex-start',
      gap: 1.5,
      py: 1.5,
    }}
  >
    <Box
      sx={{
        fontSize: '1.1rem',
        minWidth: 28,
        pt: '2px',
        textAlign: 'center',
      }}
    >
      {icon}
    </Box>
    <Box sx={{ flexGrow: 1 }}>
      <Typography
        variant="caption"
        sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', mb: 0.25 }}
      >
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 500, color: 'text.primary' }}>
        {value}
      </Typography>
    </Box>
  </Box>
);

// ─── Componente principal ─────────────────────────────────────────────────────

interface UserProfileModalProps {
  open: boolean;
  onClose: () => void;
}

export const UserProfileModal: React.FC<UserProfileModalProps> = ({ open, onClose }) => {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Obtener datos al abrir el modal
  useEffect(() => {
    if (!open) return;

    let cancelled = false;

    const fetchProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await apiClient.get<UserProfile>('/users/me');
        if (!cancelled) setProfile(data);
      } catch (err: any) {
        if (!cancelled)
          setError(err?.message || 'No se pudo obtener el perfil. Intente de nuevo.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchProfile();
    return () => { cancelled = true; };
  }, [open]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xs"
      fullWidth
      slotProps={{
        paper: {
          sx: {
            borderRadius: 4,
            backgroundColor: isDark ? '#0f1d33' : '#ffffff',
            border: '1px solid',
            borderColor: isDark
              ? 'rgba(242, 146, 0, 0.2)'
              : 'rgba(15, 29, 51, 0.1)',
            boxShadow: isDark
              ? '0 24px 64px rgba(0,0,0,0.6)'
              : '0 24px 64px rgba(15, 29, 51, 0.12)',
            overflow: 'hidden',
          },
        },
      }}
    >
      {/* ── Encabezado ── */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #F29200 0%, #e07800 100%)',
          px: 3,
          pt: 3,
          pb: 3.5,
          position: 'relative',
        }}
      >
        {/* Botón cerrar */}
        <IconButton
          onClick={onClose}
          size="small"
          aria-label="Cerrar"
          sx={{
            position: 'absolute',
            top: 10,
            right: 10,
            color: 'rgba(255,255,255,0.85)',
            '&:hover': { backgroundColor: 'rgba(255,255,255,0.15)' },
          }}
        >
          <CloseIcon sx={{ fontSize: 20 }} />
        </IconButton>

        {/* Avatar */}
        <Box
          sx={{
            width: 72,
            height: 72,
            borderRadius: '50%',
            backgroundColor: 'rgba(255,255,255,0.2)',
            border: '3px solid rgba(255,255,255,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mb: 1.5,
            mx: 'auto',
          }}
        >
          <AccountCircleIcon sx={{ fontSize: 44, color: '#fff' }} />
        </Box>

        {/* Nombre */}
        <Typography
          variant="h6"
          align="center"
          sx={{ color: '#fff', fontWeight: 700, lineHeight: 1.2 }}
        >
          {loading ? '…' : profile ? buildFullName(profile) : 'Mi Perfil'}
        </Typography>
        {profile && (
          <Typography
            variant="caption"
            align="center"
            sx={{ display: 'block', color: 'rgba(255,255,255,0.8)', mt: 0.5 }}
          >
            {profile.code}
          </Typography>
        )}
      </Box>

      <DialogTitle sx={{ display: 'none' }}>Perfil de usuario</DialogTitle>

      {/* ── Contenido ── */}
      <DialogContent sx={{ px: 3, py: 2.5 }}>
        {loading && (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4, gap: 2 }}>
            <CircularProgress color="primary" size={36} />
            <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
              Cargando perfil...
            </Typography>
          </Box>
        )}

        {!loading && error && (
          <Alert severity="error" sx={{ borderRadius: 2, my: 2 }}>
            {error}
          </Alert>
        )}

        {!loading && !error && profile && (
          <>
            {/* Estado */}
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
              <Chip
                label={profile.active ? 'Activo' : 'Inactivo'}
                color={profile.active ? 'success' : 'default'}
                size="small"
                sx={{ fontWeight: 700, px: 1 }}
              />
            </Box>

            <Divider sx={{ mb: 1 }} />

            {/* Detalles */}
            <DetailRow icon="🏷️" label="Código de empleado" value={profile.code} />
            <Divider sx={{ opacity: 0.5 }} />

            <DetailRow
              icon="📧"
              label="Correo electrónico"
              value={profile.email || <Typography variant="body2" color="text.disabled">No registrado</Typography>}
            />
            <Divider sx={{ opacity: 0.5 }} />

            <DetailRow
              icon="📱"
              label="Teléfono"
              value={profile.cellphone || <Typography variant="body2" color="text.disabled">No registrado</Typography>}
            />
            <Divider sx={{ opacity: 0.5 }} />

            <DetailRow
              icon="🪪"
              label="Documento"
              value={
                profile.document_type?.document_type
                  ? `${profile.document_type.document_type}: ${profile.document_number}`
                  : profile.document_number || '—'
              }
            />
            <Divider sx={{ opacity: 0.5 }} />

            <DetailRow
              icon="🛡️"
              label="Rol"
              value={getRoleLabel(profile)}
            />
          </>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default UserProfileModal;
