<template>
  <div class="app-container">
    <nav class="nav">
      <router-link to="/">Вопросы</router-link>
      <router-link to="/naturalLang">Естественный язык</router-link>
    </nav>
    <router-view />
  </div>
</template>

<script setup lang="ts"></script>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: Arial, sans-serif;
  background-color: #fafafa;
}

.app-container {
  min-height: 100vh;
}

.nav {
  display: flex;
  gap: 0;
  background-color: #333;
  padding: 0;
}

.nav a {
  display: block;
  padding: 14px 24px;
  color: white;
  text-decoration: none;
  font-size: 15px;
  transition: background-color 0.2s;
}

.nav a:hover {
  background-color: #555;
}

.nav a.router-link-active {
  background-color: #42b883;
}
</style>
