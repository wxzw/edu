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
  line-height: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
}

button::after {
  border: 0;
}

/* 页面入场动画 */
@keyframes page-fade-in {
  from {
    opacity: 0;
    transform: translateY(16rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-fade-enter {
  animation: page-fade-in 0.3s ease both;
}
</style>
