export default {
  stories: ["../src/**/*.stories.@(js|jsx)"],
  framework: "@storybook/react-vite",
  // Parcel's url: import scheme (asset URLs from JS) is Vite's default behaviour
  viteFinal: config => ({
    ...config,
    resolve: {
      ...config.resolve,
      alias: [...(config.resolve?.alias ?? []), { find: /^url:(.*)$/, replacement: "$1" }],
    },
  }),
}
