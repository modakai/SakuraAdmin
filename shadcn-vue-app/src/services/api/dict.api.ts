import { useMutation, useQuery } from '@tanstack/vue-query'

import type { DictOption } from '@/services/types/dict.type'
import type { IResponse } from '@/services/types/response.type'

import { useApiFetch } from '@/composables/use-fetch'

/**
 * 根据字典编码获取选项列表。
 */
export function useGetDictOptionsQuery(dictCode: string) {
  const { apiFetch } = useApiFetch()

  return useQuery<IResponse<DictOption[]>, Error>({
    queryKey: ['dict-options', dictCode],
    enabled: computed(() => Boolean(dictCode)),
    queryFn: async () => await apiFetch<IResponse<DictOption[]>>('/dict/map', {
      method: 'get',
      query: { dictCode },
    }),
  })
}

/**
 * 批量获取字典映射。
 */
export function useGetDictMapBatchMutation() {
  const { apiFetch } = useApiFetch()

  return useMutation<IResponse<Record<string, DictOption[]>>, Error, string[]>({
    mutationKey: ['dict-map-batch'],
    mutationFn: async dictCodes => await apiFetch<IResponse<Record<string, DictOption[]>>>('/dict/map/batch', {
      method: 'post',
      body: { dictCodes },
    }),
  })
}

/**
 * 根据编码和值获取字典标签。
 */
export function useGetDictLabelQuery(dictCode: string, value: string) {
  const { apiFetch } = useApiFetch()

  return useQuery<IResponse<string>, Error>({
    queryKey: ['dict-label', dictCode, value],
    enabled: computed(() => Boolean(dictCode && value)),
    queryFn: async () => await apiFetch<IResponse<string>>('/dict/label', {
      method: 'get',
      query: { dictCode, value },
    }),
  })
}
