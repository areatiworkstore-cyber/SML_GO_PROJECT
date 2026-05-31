import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import CircularProgress from '@mui/material/CircularProgress';
import type { ClientCreate } from '../types';
import { clientService } from '../services';
import { geographicService } from '../../geographic/services';
import type { DepartmentResponse, ProvinceResponse, DistrictResponse } from '../../geographic/types';
import { useNotification } from '../../../context/NotificationContext';
import { useAuth } from '../../auth';

interface ClientFormProps {
  initialData?: ClientCreate;
  clientId?: number;
  onSubmitSuccess?: () => void;
  onCancel?: () => void;
}

export const ClientForm: React.FC<ClientFormProps> = ({
  initialData,
  clientId,
  onSubmitSuccess,
  onCancel,
}) => {
  const { showSuccess, showError } = useNotification();
  const { user } = useAuth();

  const [formData, setFormData] = useState<ClientCreate>(
    initialData || {
      code: '',
      name: '',
      document_type_id: '' as unknown as number,
      document_number: '',
      address: '',
      district_id: '' as unknown as number,
      business_type_id: '' as unknown as number,
      client_group_id: '' as unknown as number,
      cellphone: '',
      observation: '',
      user_id: user?.id || 0,
    }
  );

  useEffect(() => {
    if (user?.id && !initialData) {
      setFormData((prev) => ({ ...prev, user_id: user.id }));
    }
  }, [user, initialData]);

  const [selectedDeptId, setSelectedDeptId] = useState<string | number>('');
  const [selectedProvId, setSelectedProvId] = useState<string | number>('');

  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [provinces, setProvinces] = useState<ProvinceResponse[]>([]);
  const [districts, setDistricts] = useState<DistrictResponse[]>([]);

  const [loadingGeo, setLoadingGeo] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchGeoData = async () => {
      setLoadingGeo(true);
      try {
        const [deptData, provData, distData] = await Promise.all([
          geographicService.getDepartments(),
          geographicService.getProvinces(),
          geographicService.getDistricts(),
        ]);
        setDepartments(deptData);
        setProvinces(provData);
        setDistricts(distData);
      } catch (err: any) {
        showError(err);
      } finally {
        setLoadingGeo(false);
      }
    };

    fetchGeoData();
  }, []);

  const filteredProvinces = provinces.filter((p) => p.department_id === Number(selectedDeptId));
  const filteredDistricts = districts.filter((d) => d.province_id === Number(selectedProvId));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.document_type_id) {
      showError('Por favor, seleccione el Tipo de Documento.');
      return;
    }
    if (!formData.district_id) {
      showError('Por favor, complete la ubicación geográfica hasta el Distrito.');
      return;
    }

    setSubmitting(true);
    try {
      if (clientId) {
        await clientService.updateClient(clientId, formData);
        showSuccess('¡Cliente actualizado con éxito!');
      } else {
        await clientService.createClient(formData);
        showSuccess('¡Cliente registrado con éxito en el sistema!');
      }

      setFormData({
        code: '',
        name: '',
        document_type_id: '' as unknown as number,
        document_number: '',
        address: '',
        district_id: '' as unknown as number,
        business_type_id: '' as unknown as number,
        client_group_id: '' as unknown as number,
        cellphone: '',
        observation: '',
        user_id: user?.id || 0,
      });
      setSelectedDeptId('');
      setSelectedProvId('');

      if (onSubmitSuccess) onSubmitSuccess();
    } catch (err: any) {
      showError(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box sx={{ py: 2, display: 'flex', justifyContent: 'center' }}>
      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          width: '100%',
          maxWidth: 750,
          backgroundColor: 'background.paper',
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: 4,
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.08)',
        }}
      >
        <Typography variant="h5" component="h2" sx={{ fontWeight: 'bold', color: 'primary.main', mb: 1 }}>
          Ficha de Registro de Cliente
        </Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
          Completa todos los campos obligatorios (*) para ingresar el nuevo cliente en la cartera de la fuerza de ventas.
        </Typography>

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Grid container spacing={3}>
            {/* Código y Nombre */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                required
                fullWidth
                label="Código de Cliente"
                value={formData.code}
                onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                variant="outlined"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                required
                fullWidth
                label="Razón Social / Nombre Completo"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* Tipo doc y Nro doc */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth required>
                <InputLabel id="doc-type-label">Tipo de Documento</InputLabel>
                <Select
                  labelId="doc-type-label"
                  value={formData.document_type_id || ''}
                  label="Tipo de Documento"
                  onChange={(e) => setFormData({ ...formData, document_type_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione un tipo...</em></MenuItem>
                  <MenuItem value={1}>DNI</MenuItem>
                  <MenuItem value={2}>RUC</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                required
                fullWidth
                label="Número de Documento"
                value={formData.document_number}
                onChange={(e) => setFormData({ ...formData, document_number: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* Dirección */}
            <Grid size={{ xs: 12 }}>
              <TextField
                required
                fullWidth
                multiline
                rows={2}
                label="Dirección Completa"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* Ubicación Jerárquica */}
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required>
                <InputLabel id="department-label">Departamento</InputLabel>
                <Select
                  labelId="department-label"
                  value={selectedDeptId}
                  label="Departamento"
                  onChange={(e) => {
                    setSelectedDeptId(e.target.value);
                    setSelectedProvId('');
                    setFormData({ ...formData, district_id: '' as unknown as number });
                  }}
                >
                  <MenuItem value="" disabled><em>Seleccione...</em></MenuItem>
                  {loadingGeo ? (
                    <MenuItem disabled><CircularProgress size={20} sx={{ mr: 1 }} /> Cargando...</MenuItem>
                  ) : (
                    departments.map((dept) => (
                      <MenuItem key={dept.id} value={dept.id}>{dept.name}</MenuItem>
                    ))
                  )}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required disabled={!selectedDeptId}>
                <InputLabel id="province-label">Provincia</InputLabel>
                <Select
                  labelId="province-label"
                  value={selectedProvId}
                  label="Provincia"
                  onChange={(e) => {
                    setSelectedProvId(e.target.value);
                    setFormData({ ...formData, district_id: '' as unknown as number });
                  }}
                >
                  <MenuItem value="" disabled><em>Seleccione...</em></MenuItem>
                  {filteredProvinces.map((prov) => (
                    <MenuItem key={prov.id} value={prov.id}>{prov.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required disabled={!selectedProvId}>
                <InputLabel id="district-label">Distrito</InputLabel>
                <Select
                  labelId="district-label"
                  value={formData.district_id || ''}
                  label="Distrito"
                  onChange={(e) => setFormData({ ...formData, district_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione...</em></MenuItem>
                  {filteredDistricts.map((dist) => (
                    <MenuItem key={dist.id} value={dist.id}>{dist.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Fila compacta de 3 columnas: Grupo Cliente, Giro Comercial y Celular */}
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required>
                <InputLabel id="client-group-label">Grupo Cliente</InputLabel>
                <Select
                  labelId="client-group-label"
                  value={formData.client_group_id || ''}
                  label="Grupo Cliente"
                  onChange={(e) => setFormData({ ...formData, client_group_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione un grupo...</em></MenuItem>
                  <MenuItem value={1}>B2B</MenuItem>
                  <MenuItem value={2}>B2C</MenuItem>
                  <MenuItem value={3}>B2G</MenuItem>
                  <MenuItem value={4}>C2C</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required>
                <InputLabel id="biz-type-label">Giro Comercial</InputLabel>
                <Select
                  labelId="biz-type-label"
                  value={formData.business_type_id || ''}
                  label="Giro Comercial"
                  onChange={(e) => setFormData({ ...formData, business_type_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione un giro...</em></MenuItem>
                  <MenuItem value={1}>LUBRICENTRO</MenuItem>
                  <MenuItem value={2}>TALLER MECANICO</MenuItem>
                  <MenuItem value={3}>FERRETERIA</MenuItem>
                  <MenuItem value={4}>INSTALACION ELECTRICA</MenuItem>
                  <MenuItem value={5}>INSTALACION GASISTA</MenuItem>
                  <MenuItem value={6}>PLOMERIA</MenuItem>
                  <MenuItem value={8}>OTRO</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                label="Celular"
                value={formData.cellphone || ''}
                onChange={(e) => setFormData({ ...formData, cellphone: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* Observaciones a ancho completo debajo */}
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Observación / Comentarios"
                placeholder="Ingresa aquí notas internas o incidencias relevantes sobre el cliente..."
                value={formData.observation || ''}
                onChange={(e) => setFormData({ ...formData, observation: e.target.value })}
                variant="outlined"
              />
            </Grid>
          </Grid>

          <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
            {onCancel && (
              <Button
                variant="outlined"
                color="inherit"
                fullWidth
                onClick={onCancel}
                sx={{
                  py: 1.5,
                  fontWeight: 'bold',
                  fontSize: '1rem',
                }}
              >
                Cancelar
              </Button>
            )}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              color="primary"
              disabled={submitting}
              sx={{
                py: 1.5,
                fontWeight: 'bold',
                fontSize: '1rem',
                color: 'secondary.contrastText',
                '&:hover': {
                  backgroundColor: 'primary.dark',
                },
              }}
            >
              {submitting ? <CircularProgress size={24} color="inherit" /> : (clientId ? 'Guardar Cambios' : 'Guardar Registro')}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default ClientForm;