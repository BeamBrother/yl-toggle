import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import NoData from '.';
import { IntlWrapper } from 'components/utils/wrapper';

it('NoData snapshot', async () => {
  await (async () => {
    const { asFragment } = render(
      <NoData></NoData>
    , { wrapper: IntlWrapper});
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('NoData custom text', async () => {
  await (async () => {
    render(
      <NoData text='No Data Test'></NoData>
    , { wrapper: IntlWrapper});

    screen.getByText('No Data Test');
    expect(screen.getByText('No Data Test')).not.toBeNull();
  })();
});
