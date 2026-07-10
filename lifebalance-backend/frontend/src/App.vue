<template>
  <main class="shell">
    <section>
      <p class="eyebrow">LifeBalance</p>
      <h1>Development frontend is running</h1>
      <p>API traffic is proxied to the Spring Cloud Gateway through Docker networking.</p>
    </section>
  </main>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  color: #172033;
  background: #f4f7fb;
}

section {
  width: min(720px, calc(100vw - 32px));
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  color: #2563eb;
}

h1 {
  margin: 0 0 16px;
  font-size: 40px;
  line-height: 1.1;
}

p {
  font-size: 18px;
  line-height: 1.6;
}
</style>
