<template>
  <div class="container">
    <div class="row">
      <!-- User Photo -->
      <div class="col-md-3">
        <div class="card" style="margin-top: 50px;">
          <div class="card-body">
            <img src="../../../assets/images/user_photo.jpg" alt="User Photo" class="img-fluid">
      <!-- 按钮组 -->
            <div class="d-flex flex-column align-items-center mt-3">
              <!-- 修改信息按钮 -->
              <button type="button" class="btn btn-primary w-75 shadow-sm" @click="openModal">
                ✏️ 修改信息
              </button>

              <!-- 选择背景图片按钮 -->
              <label for="background-upload" class="btn btn-outline-secondary w-75 shadow-sm mt-2">
                📷 选择背景
              </label>
              <input type="file" id="background-upload" @change="handleFileChange" accept="image/*" class="d-none" />

              <!-- 切换导航布局按钮 -->
              <button @click="toggleLayout" class="btn btn-outline-info w-75 shadow-sm mt-2">
                🔄 切换主题
              </button>
            </div>

          </div>
        </div>
      </div>

      <!-- User Information -->
      <div class="col-md-9">
        <div class="card" style="margin-top: 50px;">
          <div class="card-body">
            <h5 class="card-title">用户信息</h5>
            <table class="table table-bordered">
              <tbody>
                <tr>
                  <th scope="row">用户名</th>
                  <td>{{ username }}</td>
                </tr>
                <tr>
                  <th scope="row">电话</th>
                  <td>{{ phone }}</td>
                </tr>
                <tr>
                  <th scope="row">邮箱</th>
                  <td>{{ email }}</td>
                </tr>
                <tr>
                  <th scope="row">用户级别</th>
                  <td v-if="userLevel === '0'">普通用户</td>
                  <td v-else>管理员</td>
                </tr>
                <tr>
                  <th scope="row">个性签名</th>
                  <td>{{ signature }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit User Modal -->
    <div v-if="isModalVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="userModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="userModalLabel">编辑用户</h5>
            <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form>
              <div class="mb-3">
                <label for="phone" class="form-label">电话</label>
                <input type="text" class="form-control" id="phone" v-model="form.phone">
              </div>
              <div class="mb-3">
                <label for="email" class="form-label">邮箱</label>
                <input type="email" class="form-control" id="email" v-model="form.email">
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveChanges">保存更改</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useStore } from 'vuex'
import { ref, reactive } from 'vue';
import $ from 'jquery';

export default {
  setup() {
    const store = useStore();

    let username = store.state.user.username;
    let phone = store.state.user.phone;
    let email = store.state.user.email;
    let userLevel = store.state.user.urank;
    let signature = '生活需要一点冒险';

    // Modal visibility and form data
    const isModalVisible = ref(false);
    const form = reactive({
      id: null,
      username: '',
      phone: '',
      email: '',
      userLevel: ''
    });

    // Method to open the modal
    const openModal = () => {
      isModalVisible.value = true;
    };

    // Method to close the modal
    const closeModal = () => {
      isModalVisible.value = false;
    };

    // Method to save changes
    const saveChanges = () => {
      $.ajax({
        url: "http://127.0.0.1:3000/user/updatabyname/",
        type: "post",
        headers: {
          Authorization: "Bearer " + store.state.user.token,
        },
        data: {
          username: username, 
          phone: form.phone,
          email: form.email
        },
        success(resp) {
          console.log(resp);
          store.dispatch("getinfo", {
            success() {
              location.reload();
            }
          });
        },
        error(resp) {
          console.log(resp);
        }
      });
      closeModal();
    };

    // Handle file change (background image upload)
    const handleFileChange = (event) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          const imageUrl = e.target.result;
          document.body.style.backgroundImage = `url(${imageUrl})`; // Update background image
          document.body.style.backgroundSize = 'cover'; // Ensure it covers the screen
        };
        reader.readAsDataURL(file);
      }
    };
    const toggleLayout = () => {
        store.commit("toggleNavLayout");
    };

    return {
      username,
      phone,
      email,
      userLevel,
      signature,
      isModalVisible,
      form,
      openModal,
      closeModal,
      saveChanges,
      handleFileChange,
      toggleLayout
    };
  }
};
</script>

<style scoped>
div.error-message {
  color: red;
}
</style>
