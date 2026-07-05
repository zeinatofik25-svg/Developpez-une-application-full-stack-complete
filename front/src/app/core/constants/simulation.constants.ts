export const SIMULATION_ERROR_KEYS = {
  TOPICS_FETCH: 'topicsFetch',
  FEED_FETCH: 'feedFetch',
  POST_DETAIL_FETCH: 'postDetailFetch'
} as const;

export type SimulationErrorKey = (typeof SIMULATION_ERROR_KEYS)[keyof typeof SIMULATION_ERROR_KEYS];
