<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">欢迎使用智能体训练与仿真平台</h2>
      <div class="user-info">
        <p><strong>用户名:</strong> {{ userName }}</p>
        <p><strong>用户 ID:</strong> {{ userId }}</p>
        <p><strong>角色:</strong> {{ roleId }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useStore } from "vuex";
import { useRoute } from "vue-router";
import $ from "jquery";

const userName = ref(null);
const userId = ref(null);
const roleId = ref(null);
const router = useRoute();
const store = useStore();

onMounted(() => {
  // 从 URL 查询参数获取 username 和 userid
  userName.value = router.query.username + "_remote" || localStorage.getItem("username") || "未知用户";
  userId.value = router.query.userid || localStorage.getItem("userid") || "未知ID";
  roleId.value = router.query.roleid || localStorage.getItem("roleid") || "未知角色";
  console.log("userName : ", userName.value);
  console.log("userId : ", userId.value);
  console.log("roleId : ", roleId.value);
  
  // 自动登录
  handleLogin();
});

const handleLogin = () => {
  $.ajax({
    url: "http://127.0.0.1:3000/log/remote/",
    type: "POST",
    data: {
      userName: userName.value,
      userId: userId.value,
      roleId: roleId.value,
    },
    success(resp) {
      console.log("提交成功:", resp);
      store.dispatch("login", {
        username: userName.value,
        password: "123456", // 密码可以根据实际情况进行修改
        success() {
          store.dispatch("getinfo", {
            success() {
              router.push({ name: "home" });
            },
          });
        },
      });
    },
    error(resp) {
      console.error("提交失败:", resp);
      alert("提交失败 ❌");
    },
  });
};
</script>

<style scoped>
/* 背景样式 - 柔和渐变效果 */
@keyframes background-animation {
  0% {
    background: linear-gradient(45deg, #b3c2f2, #a1b5d8, #87a3c3);
  }
  50% {
    background: linear-gradient(45deg, #8aafc1, #9d8ec8, #6f8b9b);
  }
  100% {
    background: linear-gradient(45deg, #b3c2f2, #a1b5d8, #87a3c3);
  }
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(45deg, #b3c2f2, #a1b5d8, #87a3c3); /* 柔和的蓝色渐变背景 */
  background-size: 400% 400%;
  animation: background-animation 20s ease infinite; /* 适度的渐变动画 */
  font-family: 'Arial', sans-serif;
}

/* 登录框样式 - 半透明渐变 */
.login-box {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0.9));
  backdrop-filter: blur(10px); /* 半透明背景和模糊效果 */
  padding: 40px 50px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2); /* 增加阴影效果 */
  width: 80vw; /* 设置宽度为页面宽度的 80% */
  height: 80vh; /* 设置高度为页面高度的 80% */
  text-align: center;
  max-width: 100%;
  overflow-y: auto; /* 使内容在需要时可以滚动 */
}

/* 标题样式 */
.login-title {
  font-size: 30px;
  margin-bottom: 30px;
  color: #333;
  font-weight: bold;
  letter-spacing: 1px;
}

/* 用户信息显示 */
.user-info {
  margin-bottom: 20px;
  font-size: 16px;
  color: #555;
}

.user-info p {
  margin-bottom: 10px;
  font-weight: 500;
}

/* 信息文本 */
.user-info strong {
  color: #333;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 600px) {
  .login-box {
    width: 90%;
    padding: 30px 20px;
    height: auto;
  }

  .login-title {
    font-size: 24px;
  }
}
</style>
