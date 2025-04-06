<template>
    <ContentField>
        <div class="manual-container">
            <h2 class="manual-title">📘 用户手册</h2>
            <div class="manual-frame-wrapper">
                <iframe
                    src="/userManual.html"
                    class="manual-frame"
                    frameborder="0"
                ></iframe>
            </div>
        </div>
    </ContentField>
</template>

<script>
import ContentField from '../../components/ContentField.vue'

export default {
    components: {
        ContentField
    },
    setup() {

    }
}
</script>

<style scoped>
.manual-container {
    padding: 20px;
    background-color: #f9f9f9;
}

.manual-title {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin-bottom: 16px;
    text-align: center;
}

.manual-frame-wrapper {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    border-radius: 12px;
    overflow: hidden;
    background-color: white;
}

.manual-frame {
    width: 100%;
    height: 80vh;
    border: none;
}
</style>
