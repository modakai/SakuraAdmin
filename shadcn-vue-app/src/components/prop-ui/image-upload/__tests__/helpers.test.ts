import { describe, expect, it } from 'vitest'

import type { ImageUploadItem } from '@/services/types/file.type'

import {
  collectUploadedUrls,
  createImageUploadItem,
  getImageFileErrorMessage,
  mergeUploadedUrl,
} from '../helpers'

describe('image-upload helpers', () => {
  it('should replace previous url when maxCount is 1', () => {
    expect(mergeUploadedUrl(['https://example.com/old.png'], 'https://example.com/new.png', 1)).toEqual([
      'https://example.com/new.png',
    ])
  })

  it('should append uploaded urls in order when maxCount is greater than 1', () => {
    expect(mergeUploadedUrl(['https://example.com/1.png'], 'https://example.com/2.png', 3)).toEqual([
      'https://example.com/1.png',
      'https://example.com/2.png',
    ])
  })

  it('should block files larger than 1MB before upload', () => {
    const largeFile = new File(['x'.repeat(1024 * 1024 + 1)], 'large.png', { type: 'image/png' })

    expect(getImageFileErrorMessage(largeFile)).toBe('图片大小不能超过 1MB')
  })

  it('should block unsupported image suffix before upload', () => {
    const invalidFile = new File(['demo'], 'avatar.gif', { type: 'image/gif' })

    expect(getImageFileErrorMessage(invalidFile)).toBe('仅支持 jpeg、jpg、svg、png、webp 格式图片')
  })

  it('should only collect successful urls so failed uploads do not pollute model value', () => {
    const items: ImageUploadItem[] = [
      createImageUploadItem({ url: 'https://example.com/success.png', status: 'success' }),
      createImageUploadItem({ url: '', status: 'uploading' }),
      createImageUploadItem({ url: 'https://example.com/error.png', status: 'error' }),
    ]

    expect(collectUploadedUrls(items)).toEqual(['https://example.com/success.png'])
  })
})
