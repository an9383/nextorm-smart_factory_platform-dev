<template>
  <q-avatar color="primary" text-color="white" size="40px">
    <img v-if="image" :src="image" />
    <span v-else>{{ text[0]?.toUpperCase() }}</span>
    <q-tooltip>
      {{ text }}
    </q-tooltip>
  </q-avatar>
</template>
<script setup>
import UserService from 'src/services/system/UserService'
import { computed, onMounted, ref } from 'vue'

const props = defineProps({
  userId: {
    type: String,
    require: false,
    default: () => null,
  },
  userName: {
    type: String,
    required: false,
  },
  userImage: {
    type: String,
    require: false,
  },
})

onMounted(() => {
  if (props.userId) {
    getUserImage()
  }
})
const userProfileImage = ref(null)

const getUserImage = async () => {
  userProfileImage.value = await UserService.getUserImage(props.userId)
}

const image = computed(() => {
  return props.userImage || userProfileImage.value
})
const text = computed(() => {
  return props.userName || props.userId
})
</script>

<style scoped></style>
