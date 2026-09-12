module.exports = [
  {
    env: {
      browser: true,
      es2021: true,
      node: true
    },
    extends: [
      'eslint:recommended',
      '@vue/eslint-config-typescript',
      '@vue/eslint-config-prettier'
    ],
    parserOptions: {
      ecmaVersion: 'latest',
      parser: '@typescript-eslint/parser',
      sourceType: 'module'
    },
    plugins: [
      '@typescript-eslint',
      'vue'
    ],
    rules: {
      // Vue rules
      'vue/multi-word-component-names': ['error', {
        ignores: ['index', 'App']
      }],
      'vue/no-unused-vars': 'error',
      'vue/require-default-prop': 'off',
      'vue/require-explicit-emits': 'error',
      
      // TypeScript rules
      '@typescript-eslint/no-unused-vars': 'error',
      '@typescript-eslint/explicit-function-return-type': 'off',
      '@typescript-eslint/no-explicit-any': 'warn',
      
      // ESLint rules
      'no-console': 'warn',
      'no-debugger': 'warn',
      'no-unused-vars': 'off'
    }
  }
]