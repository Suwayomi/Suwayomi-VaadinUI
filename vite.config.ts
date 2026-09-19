import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
    server: {
        watch: {
            usePolling: true,
            interval: 100
        }
    }
});

export default overrideVaadinConfig(customConfig);
