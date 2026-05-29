import React from 'react';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { getGoogleMapsUrl } from '../utils/maps';
import type { SxProps, Theme } from '@mui/material';

interface MapButtonProps {
  latitude: number | undefined | null;
  longitude: number | undefined | null;
  label?: string;
  size?: 'small' | 'medium' | 'large';
  variant?: 'contained' | 'outlined' | 'text';
  sx?: SxProps<Theme>;
}

export const MapButton: React.FC<MapButtonProps> = ({
  latitude,
  longitude,
  label = 'Ver en Google Maps',
  size = 'small',
  variant = 'contained',
}) => {
  if (!latitude || !longitude) {
    return (
      <Typography variant="caption" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
        Sin GPS
      </Typography>
    );
  }

  const handleRedirect = () => {
    const url = getGoogleMapsUrl(latitude, longitude);
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  return (
    <Button
      variant={variant}
      color="primary"
      size={size}
      onClick={handleRedirect}
      title={`Coordenadas: ${latitude}, ${longitude}`}
      sx={{
        gap: 1,
        borderRadius: 2,
        fontWeight: 'bold',
        px: size === 'small' ? 1.5 : 2.5,
        backgroundColor: variant === 'contained' ? '#F29200' : undefined,
        color: variant === 'contained' ? '#0F1D33' : '#F29200',
        '&:hover': {
          backgroundColor: variant === 'contained' ? '#d88200' : 'rgba(242, 146, 0, 0.08)',
        },
      }}
    >
      <svg
        className="w-4 h-4"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        width="16"
        height="16"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2.5}
          d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
        />
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2.5}
          d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
        />
      </svg>
      {label}
    </Button>
  );
};
export default MapButton;
