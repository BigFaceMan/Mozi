<template>
    <ContentField>
        <div>
            <p>用户名: {{ username }}</p>
            <p>用户 ID: {{ userid }}</p>
        </div>
    </ContentField>
</template>

<script setup>
import { ref, onMounted } from "vue";
import ContentField from '../../components/ContentField.vue';
import { useStore } from 'vuex'
import $ from 'jquery';
import { useRoute } from "vue-router";

const userName = ref(null);
const userId = ref(null);
const router = useRoute();
const store = useStore();

onMounted(() => {
    // 从 URL 查询参数获取 username 和 userid
    userName.value = router.query.username + "_remote" || localStorage.getItem("username") || "未知用户";
    userId.value = router.query.userid || localStorage.getItem("userid") || "未知ID";
    console.log("userName : ", userName.value)
    console.log("userId : ", userId.value)

    $.ajax({
        url: "http://127.0.0.1:3000/log/remote/",
        type: "POST",
        data: {
            userName: userName.value,
            userId: userId.value
        },
        success(resp) {
            console.log("提交成功:", resp);
            store.dispatch("login", {
                username: userName.value,
                password: "123456",
                success() {
                    store.dispatch("getinfo", {
                        success() {
                            router.push({ name: 'home' });
                        }
                    })
                }
            })
        },
        error(resp) {
            console.error("提交失败:", resp);
            alert("提交失败 ❌");
        }
    });
});
</script>

<style scoped>
p {
    font-size: 16px;
    color: #333;
}
</style>
