import { useMutation } from '@tanstack/vue-query'

import { useApiFetch } from '@/composables/use-fetch'
import type { IResponse } from '@/services/types/response.type'

/**
 * 上传文件到后端 OSS 接口。
 */
export function useUploadFileMutation() {
  const { apiFetch } = useApiFetch()

  return useMutation<IResponse<string>, Error, File>({
    mutationKey: ['file-upload'],
    mutationFn: async (file) => {
      const formData = new FormData()
      formData.append('file', file)

      return await apiFetch<IResponse<string>>('/file/upload', {
        method: 'post',
        body: formData,
      })
    },
  })
}
