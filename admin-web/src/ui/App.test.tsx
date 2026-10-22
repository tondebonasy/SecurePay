import React from "react";
import {
  render,
  screen,
  fireEvent,
  cleanup,
  waitFor,
} from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, it, expect, vi } from "vitest";
import App from "./App";
import { api } from "../util/api";
vi.mock("../util/api", () => ({ api: { get: vi.fn(), post: vi.fn() } }));
afterEach(() => {
  cleanup();
  sessionStorage.clear();
  localStorage.clear();
  vi.clearAllMocks();
});
it("mounts the protected dashboard immediately after successful sign-in", async () => {
  localStorage.setItem("securepay_device_id_v1", "test-device");
  vi.mocked(api.post).mockResolvedValue({
    data: {
      data: {
        accessToken: "test-token",
        refreshToken: "test-refresh",
        sessionId: "test-session",
      },
    },
  });
  vi.mocked(api.get).mockImplementation(
    async (url: any) =>
      ({
        headers: {},
        data: {
          data: url.includes("health")
            ? { status: "ok" }
            : {
                currency: "USD",
                totalValue: 0,
                totalTx: 0,
                successful: 0,
                pending: 0,
                failed: 0,
                successRate: 0,
                daily: {},
                statuses: {},
                methods: {},
              },
        },
      }) as any,
  );
  render(
    <MemoryRouter initialEntries={["/login"]}>
      <App />
    </MemoryRouter>,
  );
  fireEvent.change(screen.getByLabelText(/Phone/), {
    target: { value: "+263770000000" },
  });
  fireEvent.click(screen.getByRole("button", { name: "Continue" }));
  fireEvent.change(screen.getByLabelText(/Password/), {
    target: { value: "IntegrationOnly!2026" },
  });
  fireEvent.click(screen.getByRole("button", { name: "Sign in" }));
  await waitFor(
    () =>
      expect(api.post).toHaveBeenCalledWith("/v1/auth/login", {
        phone: "+263770000000",
        password: "IntegrationOnly!2026",
      }),
    { timeout: 5000 },
  );
  expect(
    await screen.findByRole(
      "heading",
      { name: "Overview", level: 1 },
      { timeout: 5000 },
    ),
  ).toBeTruthy();
  expect(screen.queryByRole("button", { name: "Sign in" })).toBeNull();
});
it("redirects unauthenticated visitors to sign-in", async () => {
  render(
    <MemoryRouter initialEntries={["/"]}>
      <App />
    </MemoryRouter>,
  );
  expect(await screen.findByRole("button", { name: "Continue" })).toBeTruthy();
});

it("explains an unavailable API without hiding the sign-in form", async () => {
  vi.mocked(api.post).mockRejectedValue(new Error("Network Error"));
  render(
    <MemoryRouter initialEntries={["/login"]}>
      <App />
    </MemoryRouter>,
  );
  fireEvent.change(screen.getByLabelText("Phone number"), {
    target: { value: "+263770000000" },
  });
  fireEvent.click(screen.getByRole("button", { name: "Continue" }));
  fireEvent.change(screen.getByLabelText("Password"), {
    target: { value: "IntegrationOnly!2026" },
  });
  fireEvent.click(screen.getByRole("button", { name: "Sign in" }));
  expect(
    await screen.findByRole("alert", {}, { timeout: 5000 }),
  ).toHaveProperty(
    "textContent",
    "Cannot reach SecurePay. Check that the API is running and try again.",
  );
  expect(screen.getByLabelText("Password")).toBeTruthy();
});
