import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'

import type { IResponse } from '@/services/types/response.type'
import type {
  UserAddForm,
  UserItem,
  UserQuery,
  UserUpdateForm,
  UserUpdateMyForm,
} from '@/services/types/user.type'

import { useApiFetch } from '@/composables/use-fetch'

/**
 * 通用分页响应。
 */
interface PageResponse<T> {
  records: T[]
  totalRow: number
  pageSize: number
  pageNumber: number
}

/**
 * 获取后台用户分页列表。
 */
export function useGetUserPageQuery(query: UserQuery) {
  const { apiFetch } = useApiFetch()

  return useQuery<IResponse<PageResponse<UserItem>>, Error>({
    queryKey: ['user-page', query.page, query.pageSize, query.userName, query.userRole],
    queryFn: async () => await apiFetch<IResponse<PageResponse<UserItem>>>('/user/list/page', {
      method: 'post',
      body: query,
    }),
  })
}

/**
 * 获取后台用户详情。
 */
export function useGetUserDetailQuery(id: number | null | undefined) {
  const { apiFetch } = useApiFetch()

  return useQuery<IResponse<UserItem>, Error>({
    queryKey: ['user-detail', id],
    enabled: computed(() => Boolean(id)),
    queryFn: async () => await apiFetch<IResponse<UserItem>>('/user/get', {
      method: 'get',
      query: { id },
    }),
  })
}

/**
 * 新增后台用户。
 */
export function useCreateUserMutation() {
  const { apiFetch } = useApiFetch()
  const queryClient = useQueryClient()

  return useMutation<IResponse<number>, Error, UserAddForm>({
    mutationKey: ['user-create'],
    mutationFn: async data => await apiFetch<IResponse<number>>('/user/add', {
      method: 'post',
      body: data,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-page'] })
    },
  })
}

/**
 * 更新后台用户。
 */
export function useUpdateUserMutation() {
  const { apiFetch } = useApiFetch()
  const queryClient = useQueryClient()

  return useMutation<IResponse<boolean>, Error, UserUpdateForm>({
    mutationKey: ['user-update'],
    mutationFn: async data => await apiFetch<IResponse<boolean>>('/user/update', {
      method: 'post',
      body: data,
    }),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['user-page'] })
      queryClient.invalidateQueries({ queryKey: ['user-detail', variables.id] })
    },
  })
}

/**
 * 删除后台用户。
 */
export function useDeleteUserMutation() {
  const { apiFetch } = useApiFetch()
  const queryClient = useQueryClient()

  return useMutation<IResponse<boolean>, Error, number>({
    mutationKey: ['user-delete'],
    mutationFn: async id => await apiFetch<IResponse<boolean>>('/user/delete', {
      method: 'post',
      body: { id },
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-page'] })
    },
  })
}

/**
 * 获取公开用户详情。
 */
export function useGetUserVOQuery(id: number | null | undefined) {
  const { apiFetch } = useApiFetch()

  return useQuery<IResponse<UserItem>, Error>({
    queryKey: ['user-vo', id],
    enabled: computed(() => Boolean(id)),
    queryFn: async () => await apiFetch<IResponse<UserItem>>('/user/get/vo', {
      method: 'get',
      query: { id },
    }),
  })
}

/**
 * 获取公开用户分页列表。
 */
export function useGetUserVOPageQuery(query: UserQuery) {
  const { apiFetch } = useApiFetch()

  return useQuery<IResponse<PageResponse<UserItem>>, Error>({
    queryKey: ['user-vo-page', query.page, query.pageSize, query.userName],
    queryFn: async () => await apiFetch<IResponse<PageResponse<UserItem>>>('/user/list/page/vo', {
      method: 'post',
      body: query,
    }),
  })
}

/**
 * 更新当前登录用户个人信息。
 */
export function useUpdateMyUserMutation() {
  const { apiFetch } = useApiFetch()

  return useMutation<IResponse<boolean>, Error, UserUpdateMyForm>({
    mutationKey: ['user-update-my'],
    mutationFn: async data => await apiFetch<IResponse<boolean>>('/user/update/my', {
      method: 'post',
      body: data,
    }),
  })
}
