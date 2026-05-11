import { useEffect, useState } from 'react'

// 값이 변경되면 일정 시간(delay) 후 갱신되는 debounced 값을 반환하는 커스텀 훅
// 사용자가 빠르게 연속 입력 중일 때는 이전 setTimeout을 cleanup으로 취소하므로
// "마지막 입력 후 delay가 지나야" debounced 값이 갱신됨
// → API 호출을 이 debounced 값에 묶으면 입력 끝난 후 한 번만 호출됨
export function useDebounce(value, delay = 500) {
    const [debouncedValue, setDebouncedValue] = useState(value)

    useEffect(() => {
        const timer = setTimeout(() => setDebouncedValue(value), delay)
        // value가 또 바뀌거나 컴포넌트가 unmount되면 이전 timer 취소
        return () => clearTimeout(timer)
    }, [value, delay])

    return debouncedValue
}
