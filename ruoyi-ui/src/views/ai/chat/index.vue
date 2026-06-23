<template>
  <div class="app-container ai-chat-page">
    <div class="chat-shell">
      <div class="chat-toolbar">
        <div class="toolbar-title">
          <i class="el-icon-chat-dot-round"></i>
          <span>对话</span>
        </div>
        <div class="toolbar-actions">
          <el-switch
            v-model="ragEnabled"
            size="small"
            active-text="知识库增强"
          />
          <el-switch
            v-model="agentEnabled"
            size="small"
            active-text="ERP工具"
          />
          <el-select
            v-model="selectedModel"
            size="small"
            filterable
            placeholder="选择模型"
            :loading="modelLoading"
            class="model-select"
          >
            <el-option
              v-for="model in modelOptions"
              :key="model"
              :label="model"
              :value="model"
            />
          </el-select>
          <el-button size="small" icon="el-icon-refresh" circle @click="loadModels"></el-button>
          <el-button size="small" icon="el-icon-delete" circle @click="clearMessages"></el-button>
        </div>
      </div>

      <div ref="messageList" class="message-list">
        <div
          v-for="item in messages"
          :key="item.id"
          :class="['message-row', item.role]"
        >
          <div class="message-avatar">
            <i :class="item.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
          </div>
          <div class="message-bubble">
            <div class="message-meta">
              <span>{{ item.role === 'user' ? '我' : selectedModel || 'AI' }}</span>
              <span>{{ item.time }}</span>
            </div>
            <div class="message-content">{{ item.content }}</div>
            <div v-if="item.toolCalls && item.toolCalls.length" class="tool-traces">
              <div v-for="tool in item.toolCalls" :key="tool.toolName" class="tool-trace">
                <i class="el-icon-connection"></i>
                <span>已调用 {{ tool.toolName }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="loading" class="message-row assistant">
          <div class="message-avatar">
            <i class="el-icon-cpu"></i>
          </div>
          <div class="message-bubble loading-bubble">
            <i class="el-icon-loading"></i>
            <span>生成中</span>
          </div>
        </div>
      </div>

      <div class="composer">
        <el-input
          ref="promptInput"
          v-model="prompt"
          type="textarea"
          :rows="4"
          resize="none"
          maxlength="4000"
          show-word-limit
          placeholder="输入消息"
          @keydown.native="handleKeydown"
        />
        <div class="composer-actions">
          <el-button size="small" icon="el-icon-document-copy" @click="copyLastAnswer" :disabled="!lastAnswer">复制回复</el-button>
          <el-button type="primary" size="small" icon="el-icon-position" :loading="loading" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { chat, listModels } from '@/api/ai/chat'
import cache from '@/plugins/cache'

const CHAT_STATE_KEY = 'ai-chat-state'

export default {
  name: 'AiChat',
  data() {
    return {
      prompt: '',
      selectedModel: '',
      modelOptions: [],
      ragEnabled: true,
      agentEnabled: true,
      modelLoading: false,
      loading: false,
      messages: [
        {
          id: Date.now(),
          role: 'assistant',
          content: '你好，我可以帮你处理 ERP 业务问题、整理数据口径、生成操作建议。',
          time: this.formatTime(new Date())
        }
      ]
    }
  },
  computed: {
    lastAnswer() {
      const answers = this.messages.filter(item => item.role === 'assistant')
      return answers.length ? answers[answers.length - 1].content : ''
    }
  },
  created() {
    this.restoreState()
    this.loadModels()
  },
  watch: {
    prompt() {
      this.saveState()
    },
    selectedModel() {
      this.saveState()
    },
    ragEnabled() {
      this.saveState()
    },
    agentEnabled() {
      this.saveState()
    },
    messages: {
      deep: true,
      handler() {
        this.saveState()
      }
    }
  },
  methods: {
    restoreState() {
      const state = cache.session.getJSON(CHAT_STATE_KEY)
      if (!state) {
        return
      }
      this.prompt = state.prompt || ''
      this.selectedModel = state.selectedModel || ''
      this.ragEnabled = state.ragEnabled !== undefined ? state.ragEnabled : true
      this.agentEnabled = state.agentEnabled !== undefined ? state.agentEnabled : true
      this.messages = Array.isArray(state.messages) && state.messages.length ? state.messages : this.messages
      this.scrollToBottom()
    },
    saveState() {
      cache.session.setJSON(CHAT_STATE_KEY, {
        prompt: this.prompt,
        selectedModel: this.selectedModel,
        ragEnabled: this.ragEnabled,
        agentEnabled: this.agentEnabled,
        messages: this.messages
      })
    },
    loadModels() {
      this.modelLoading = true
      listModels().then(response => {
        this.modelOptions = response.data || []
        if (this.modelOptions.length && (!this.selectedModel || this.modelOptions.indexOf(this.selectedModel) === -1)) {
          this.selectedModel = this.modelOptions[0]
        }
      }).finally(() => {
        this.modelLoading = false
      })
    },
    sendMessage() {
      const text = this.prompt.trim()
      if (!text || this.loading) {
        return
      }
      this.messages.push(this.createMessage('user', text))
      this.prompt = ''
      this.loading = true
      this.scrollToBottom()

      chat({
        prompt: text,
        model: this.selectedModel,
        ragEnabled: this.ragEnabled,
        agentEnabled: this.agentEnabled
      }).then(response => {
        const data = response.data || {}
        const message = this.createMessage('assistant', data.content || '', data.toolCalls || [])
        this.messages.push(message)
      }).finally(() => {
        this.loading = false
        this.scrollToBottom()
        this.$nextTick(() => {
          this.$refs.promptInput && this.$refs.promptInput.focus()
        })
      })
    },
    clearMessages() {
      this.messages = []
      this.prompt = ''
      this.saveState()
    },
    copyLastAnswer() {
      const text = this.lastAnswer
      if (!text) {
        return
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(() => {
          this.$modal.msgSuccess('已复制')
        })
      } else {
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        this.$modal.msgSuccess('已复制')
      }
    },
    handleKeydown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        this.sendMessage()
      }
    },
    createMessage(role, content, toolCalls) {
      return {
        id: Date.now() + Math.random(),
        role,
        content,
        toolCalls: toolCalls || [],
        time: this.formatTime(new Date())
      }
    },
    formatTime(date) {
      const pad = value => String(value).padStart(2, '0')
      return `${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.messageList
        if (el) {
          el.scrollTop = el.scrollHeight
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-chat-page {
  height: calc(100vh - 84px);
  min-height: 560px;
  padding: 16px;
  background: #f3f6f8;
}

.chat-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: #ffffff;
  border: 1px solid #d9e2e7;
  border-radius: 8px;
  overflow: hidden;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  padding: 10px 16px;
  border-bottom: 1px solid #e7edf1;
  background: #fbfcfd;
}

.toolbar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}

.toolbar-title i {
  color: #246b56;
  font-size: 20px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-select {
  width: 220px;
}

.message-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 18px;
  background: #eef3f5;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.message-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  border-radius: 50%;
  background: #ffffff;
  border: 1px solid #d4dee5;
  color: #51606d;
}

.message-row.user .message-avatar {
  background: #246b56;
  border-color: #246b56;
  color: #ffffff;
}

.message-bubble {
  max-width: min(760px, 76%);
  padding: 10px 12px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dce5ea;
  color: #263238;
  box-shadow: 0 2px 6px rgba(31, 45, 61, 0.04);
}

.message-row.user .message-bubble {
  background: #e7f3ee;
  border-color: #c7dfd4;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 6px;
  color: #7b8790;
  font-size: 12px;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  font-size: 14px;
}

.tool-traces {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.tool-trace {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 4px;
  background: #edf4ff;
  color: #2b5f9e;
  font-size: 12px;
}

.loading-bubble {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #52616d;
}

.composer {
  padding: 12px 16px;
  border-top: 1px solid #e7edf1;
  background: #ffffff;
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .ai-chat-page {
    height: calc(100vh - 64px);
    padding: 8px;
  }

  .chat-toolbar {
    align-items: stretch;
    flex-direction: column;
    gap: 10px;
  }

  .toolbar-actions {
    justify-content: space-between;
  }

  .model-select {
    flex: 1;
    width: auto;
  }

  .message-bubble {
    max-width: 82%;
  }
}
</style>
