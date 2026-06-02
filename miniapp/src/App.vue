<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app';
import { useAuthStore } from '@/stores/auth';

onLaunch(() => {
  useAuthStore().hydrate();
});
</script>

<style>
page {
  min-height: 100%;
  background: #f6f1e8;
  color: #17211d;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

view,
text,
button,
input {
  box-sizing: border-box;
}

button {
  padding: 0;
  margin: 0;
  border: 0;
  background: transparent;
  line-height: 1;
}

button::after {
  border: 0;
}
</style>
