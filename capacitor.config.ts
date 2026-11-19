import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.parv.health',
  appName: 'Parv Health',
  webDir: 'dist',
  server: {
    androidScheme: 'https'
  }
};

export default config;
