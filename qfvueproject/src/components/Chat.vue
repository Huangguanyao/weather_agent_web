<template>
  <div class="chat-container">
    <!-- 聊天窗口 -->
    <div class="chat-box" ref="chatBox">
      <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.sender]">
        <!-- 消息内容 -->
        <div class="message-content-wrapper">
          <!-- 头像 -->
          <img
              :src="msg.sender === 'user' ? userAvatar : botAvatar"
              alt="avatar"
              :class="['avatar', msg.sender]"
          />
          <!-- 消息内容 -->
          <div class="message-content">
            <!-- 如果是系统消息，解析 <think> 标签并渲染 Markdown -->
            <span v-if="msg.sender === 'bot'">
              <span
                  v-for="(part, idx) in parseMessageContent(msg.content)"
                  :key="idx"
                  :class="part.isThink && part.text.trim() ? 'think-content' : ''"
              >
                <span v-if="part.isThink && part.text.trim()">
                  <!-- 渲染思考内容的 Markdown -->
                  <span v-html="renderMarkdown(part.text)"></span>
                </span>
                <span v-else>
                  <!-- 渲染普通内容的 Markdown -->
                  <span v-html="renderMarkdown(part.text)"></span>
                </span>
                <br v-if="part.isThink && part.text.trim()"> <!-- 在思考内容后添加换行 -->
              </span>
            </span>
            <!-- 如果不是系统消息，直接显示用户输入的内容 -->
            <span v-else>{{ msg.content }}</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 文件上传 -->
      <el-upload
          class="upload-btn"
          action="#"
          :before-upload="handleFileUpload"
          :show-file-list="false"
      >
        <el-button type="primary" size="small">上传文件</el-button>
      </el-upload>

      <!-- 消息输入框 -->
      <el-input
          v-model="inputMessage"
          placeholder="请输入消息"
          class="message-input"
          @keyup.enter="sendMessage"
      ></el-input>

      <!-- 发送按钮 -->
      <el-button class="el-button" type="primary" size="small" @click="sendMessage">发送</el-button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import axios from "axios";
import { marked } from "marked"; // 引入 marked 库
import { ElButton,ElInput,ElUpload } from 'element-plus';
export default {
  components: {
    ElButton,
    ElInput,
    ElUpload
  },
  name: "Chat",
  setup() {
    const inputMessage = ref("");
    const messages = ref([]);
    const userAvatar = ref("/images/user-avatar.png");
    const botAvatar = ref("/images/bot-avatar.png");

    // 渲染 Markdown 内容
    const renderMarkdown = (content) => {
      return marked(content); // 将 Markdown 转换为 HTML
    };

    // 解析消息内容，分离 <think> 标签
    const parseMessageContent = (content) => {
      const parts = [];
      let remaining = content;

      while (remaining.includes("<think>") && remaining.includes("</think>")) {
        const thinkStart = remaining.indexOf("<think>");
        const thinkEnd = remaining.indexOf("</think>") + "</think>".length;
        // 添加普通内容 (<think>之前的部分)
        if (thinkStart > 0) {
          parts.push({ text: remaining.slice(0, thinkStart), isThink: false });
        }

        // 提取 <think> 内容
        const thinkContent = remaining.slice(thinkStart + "<think>".length, thinkEnd - "</think>".length);
        parts.push({ text: thinkContent, isThink: true });

        // 剩余部分
        remaining = remaining.slice(thinkEnd);
      }

      // 添加剩余的普通内容
      if (remaining) {
        parts.push({ text: remaining, isThink: false });
      }

      return parts;

    }
    // 发送消息并流式接收 AI 回复
    const sendMessage = () => {
      if (!inputMessage.value.trim()) return;

      // 1. 先存储用户消息
      const userMsg = { sender: "user", content: inputMessage.value };
      messages.value.push(userMsg);

      const userText = inputMessage.value;
      inputMessage.value = ""; // 清空输入框

      // 2. 添加一个空的 AI 消息
      const botMsg = { sender: "bot", content: "" };
      messages.value.push(botMsg);

      // 3. 使用 EventSource 流式接收数据
      const eventSource = new EventSource(`http://localhost:8080/chat?prompt=${encodeURIComponent(userText)}`);

      // 4. 监听流式返回数据，拼接 AI 回复
      eventSource.onmessage = (event) => {
        console.log(event.data);
        botMsg.content += event.data; // 拼接内容

        // 触发视图更新
        messages.value = [...messages.value];
      };

      // 5. 监听错误，关闭连接
      eventSource.onerror = () => {
        eventSource.close();
      };
    };

    // 组件加载时获取历史聊天记录
    const loadHistory = async () => {
      try {
        const response = await axios.get("http://localhost:8080/messages");
        messages.value = response.data.sort((a, b) => new Date(a.created_at) - new Date(b.created_at));
      } catch (error) {
        console.error("获取历史消息失败:", error);
      }
    };

    onMounted(loadHistory);

    return {
      inputMessage,
      messages,
      userAvatar,
      botAvatar,
      sendMessage,
      parseMessageContent,
      renderMarkdown, // 返回渲染 Markdown 的方法
    };
  }



};
</script>

<style scoped>
/* 全局样式 */
html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
}

/* 聊天容器样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 16px); /* 减去一点边距 */
  width: 100%;
  max-width: 600px;
  margin: auto;
  border: 1px solid #ccc;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
}

/* 聊天框样式 */
.chat-box {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background-color: #f5f5f5;
}

/* 消息样式 */
.message {
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-start; /* 默认左对齐 */
  align-items: flex-start;
}

/* 用户消息右对齐 */
.message.user {
  justify-content: flex-end;
}

/* 消息内容包裹层样式 */
.message-content-wrapper {
  display: flex;
  align-items: flex-start;
}

/* 头像样式 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin: 0 10px; /* 设置头像与消息之间的间距 */
}

/* 消息内容样式 */
.message-content {
  max-width: 70%;
  word-wrap: break-word;
  background-color: #fff;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 用户消息内容样式 */
.message.user .message-content {
  background-color: #dcf8c6; /* 用户消息背景色 */
}

/* 思考内容样式 */
.think-content {
  color: #888; /* 思考内容颜色 */
  font-style: italic; /* 斜体显示 */
}

/* 输入区域样式 */
.input-area {
  display: flex;
  align-items: center;
  padding: 10px;
  border-top: 1px solid #ccc;
  background-color: #fff;
}

/* 上传按钮样式 */
.upload-btn {
  margin-right: 10px;
}

/* 消息输入框样式 */
.message-input {
  flex: 1;
  margin-right: 10px;
}

/* 发送按钮样式 */
.el-button {
  background-color: #409eff;
  border-color: #409eff;
}

.el-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}
</style>
