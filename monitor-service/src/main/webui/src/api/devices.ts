import { apiFetch } from './client';
import type { Device, DeviceStatus } from '../types';

export const getDevices = (): Promise<Device[]> =>
  apiFetch<Device[]>('/api/devices');

export const getDevice = (id: number): Promise<Device> =>
  apiFetch<Device>(`/api/devices/${id}`);

export const getDeviceByHardwareId = (hardwareId: string): Promise<Device> =>
  apiFetch<Device>(`/api/devices/hardware/${encodeURIComponent(hardwareId)}`);

export const getDevicesByStatus = (status: DeviceStatus): Promise<Device[]> =>
  apiFetch<Device[]>(`/api/devices/status/${status}`);
