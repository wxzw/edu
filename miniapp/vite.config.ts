import { fileURLToPath, URL } from 'node:url';
import uniPluginModule from '@dcloudio/vite-plugin-uni';
import { defineConfig } from 'vite';

type UniPluginFactory = typeof uniPluginModule;

const uni = (
  typeof uniPluginModule === 'function'
    ? uniPluginModule
    : (uniPluginModule as unknown as { default: UniPluginFactory }).default
);

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});
